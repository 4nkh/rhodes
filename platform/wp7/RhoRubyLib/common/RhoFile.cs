﻿using System;
using System.IO.IsolatedStorage;
using System.Windows;
using System.Windows.Resources;
using System.IO;

namespace rho.common
{
    public class CRhoFile
    {
        public enum EOpenModes{ OpenForAppend = 1, OpenReadOnly = 2, OpenForWrite = 3, OpenForReadWrite = 4 };

        public bool isOpened() { return false; }
        public bool open(String szFilePath, EOpenModes eMode) { return false; }
        public int write(byte[] data, int len) { return 0; }
        public int writeString(String data) { return data.Length; }
        public void flush() { }
        public void close() { }
        public void movePosToStart() { }
        public void movePosToEnd() { }
        public void setPosTo(int nPos) { }
        public int size() { return 0; }

        public String readString() { return ""; }

        //int readByte() { return 0;  }
        //int readData(void* buffer, int bufOffset, int bytesToRead);

        public static void deleteFile(String path)
        {
            using (IsolatedStorageFile isoStore = IsolatedStorageFile.GetUserStoreForApplication())
            {
                isoStore.DeleteFile(path);
            }
        }

        public static void renameFile(String oldName, String newName)
        {
            using (var store = IsolatedStorageFile.GetUserStoreForApplication())
            using (var readStream = new IsolatedStorageFileStream(oldName, FileMode.Open, store))
            using (var writeStream = new IsolatedStorageFileStream(newName, FileMode.Create, store))
            using (var reader = new BinaryReader(readStream))
            using (var writer = new BinaryWriter(writeStream))
            {
                writer.Write(reader.ReadBytes((int)readStream.Length));
            }

            using (var store1 = IsolatedStorageFile.GetUserStoreForApplication())
            {
                store1.DeleteFile(oldName);
            }
        }

        public static void deleteDirectory(String path)
        {
            using (IsolatedStorageFile isoStore = IsolatedStorageFile.GetUserStoreForApplication())
            {
                isoStore.DeleteDirectory(CFilePath.removeLastSlash(path));
            }
        }

        public static bool isDirectoryExist(String path)
        {
            using (IsolatedStorageFile isoStore = IsolatedStorageFile.GetUserStoreForApplication())
            {
                return isoStore.DirectoryExists(path);
            }
        }

        public static bool isFileExist(String path)
        {
            using (IsolatedStorageFile isoStore = IsolatedStorageFile.GetUserStoreForApplication())
            {
                return isoStore.FileExists(path);
            }
        }

        public static bool isResourceFileExist(String path)
        {
            StreamResourceInfo sr = Application.GetResourceStream(new Uri(CFilePath.removeFirstSlash(path), UriKind.Relative));
            if (sr == null)
                return false;

            return sr != null;
        }

        public static void recursiveCreateDir(String strPath)
        {
            char[] sep = { '/' };
            string[] dirsPath = strPath.Split(sep, StringSplitOptions.RemoveEmptyEntries);
            string strBaseDir = string.Empty;

            using (IsolatedStorageFile isoStore = IsolatedStorageFile.GetUserStoreForApplication())
            {
                for (int i = 0; i < dirsPath.Length - 1; i++)
                {
                    strBaseDir = System.IO.Path.Combine(strBaseDir, dirsPath[i]);
                    isoStore.CreateDirectory(strBaseDir);
                }
            }
        }

        public static string[] enumDirectory(String path)
        {
            using (IsolatedStorageFile isoStore = IsolatedStorageFile.GetUserStoreForApplication())
            {
                return isoStore.GetFileNames(CFilePath.join(path, "*"));
            }
        }

        public static void deleteFilesInFolder(String path)
        {
            using (IsolatedStorageFile isoStore = IsolatedStorageFile.GetUserStoreForApplication())
            {
                string[] arFiles = isoStore.GetFileNames(CFilePath.join(path, "*"));
                foreach (string strFile in arFiles)
                {
                    isoStore.DeleteFile(CFilePath.join(path, strFile));
                }
            }
        }

        public static void createDirectory(string path)
        {
            using (IsolatedStorageFile isoStore = IsolatedStorageFile.GetUserStoreForApplication())
            {
                isoStore.CreateDirectory(CFilePath.removeLastSlash(path));
            }
        }

        public static String readStringFromFile(String path)
        {
            string content = "";
            path = CFilePath.removeFirstSlash(path);

            if (!isFileExist(path)) return content;

            using (IsolatedStorageFile isoStore = IsolatedStorageFile.GetUserStoreForApplication())
            using (Stream st = isoStore.OpenFile(path, FileMode.Open, FileAccess.Read, FileShare.None))
            using (System.IO.BinaryReader br = new BinaryReader(st))
            {
                content = br.ReadString();
            }

            return content;
        }

        public static String readStringFromResourceFile(String path)
        {
            string content = "";
            path = CFilePath.removeFirstSlash(path);

            if (!CRhoFile.isResourceFileExist(path)) 
                return content;

            StreamResourceInfo sr = Application.GetResourceStream(new Uri(path, UriKind.Relative));

            using (System.IO.BinaryReader br = new BinaryReader(sr.Stream))
            {
                char[] str = br.ReadChars((int)sr.Stream.Length);
                content = new string(str);
            }

            return content;
        }

        public static byte[] readResourceFile(String path)
        {
            byte[] content = new byte[0];
            path = CFilePath.removeFirstSlash(path);

            if (!CRhoFile.isResourceFileExist(path))
                return content;

            StreamResourceInfo sr = Application.GetResourceStream(new Uri(path, UriKind.Relative));

            using (System.IO.BinaryReader br = new BinaryReader(sr.Stream))
            {
                content = br.ReadBytes((int)sr.Stream.Length);
            }

            return content;
        }

        public static void writeStringToFile(String strPath, String strData)
        {
            using (IsolatedStorageFile isoStore = IsolatedStorageFile.GetUserStoreForApplication())
            using (var file = isoStore.OpenFile(strPath, FileMode.Create, FileAccess.Write, FileShare.Read))
            using (BinaryWriter bw = new BinaryWriter(file))
            {
                bw.Write(strData);
            }
        }

        public static void writeDataToFile(String strPath, byte[] data)
        {
            using (IsolatedStorageFile isoStore = IsolatedStorageFile.GetUserStoreForApplication())
            using (var file = isoStore.OpenFile(strPath, FileMode.Create, FileAccess.Write, FileShare.Read))
            using (BinaryWriter bw = new BinaryWriter(file))
            {
                bw.Write(data);
            }
        }
    }
}
