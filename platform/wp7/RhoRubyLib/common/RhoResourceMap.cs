﻿using System;
using System.Net;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Documents;
using System.Windows.Ink;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Animation;
using System.Windows.Shapes;

namespace rho.common
{
    public sealed class CRhoResourceMap
    {
        private static CRhodesApp RHODESAPP() { return CRhodesApp.Instance; }

        private static void clearMap(String strMap)
        {
            string[] files = strMap.Split('\n');

            for (int i = 0; i < files.Length - 1; i++)
            {
                String[] values = files[i].Split('|');

                String strFile = RHODESAPP().canonicalizeRhoUrl(values[0]);
                if (CRhoFile.isFileExist(strFile))
                {
                    CRhoFile.deleteFile(strFile);
                }
            } 
        }

        private static void copyMap(String strMap)
        {
            string[] files = strMap.Split('\n');

            for (int i = 0; i < files.Length - 1; i++)
            {
                String[] values = files[i].Split('|');

                String strFile = RHODESAPP().canonicalizeRhoUrl(values[0]);
                CRhoFile.recursiveCreateDir(RHODESAPP().canonicalizeRhoUrl(strFile));
                CRhoFile.writeDataToFile(strFile, CRhoFile.readResourceFile(strFile));
            }

            CRhoFile.writeStringToFile(RHODESAPP().canonicalizeRhoUrl("RhoBundleMap.txt"), strMap);
        }

        public static void deployContent() 
        {
            String newMap = CRhoFile.readStringFromResourceFile(RHODESAPP().canonicalizeRhoUrl("RhoBundleMap.txt"));
            String curMap = CRhoFile.readStringFromFile(RHODESAPP().canonicalizeRhoUrl("RhoBundleMap.txt"));

            if (curMap == "")
            {
                copyMap(newMap);
            }
            else if (curMap != newMap)
            {
                clearMap(curMap);
                copyMap(newMap);   
            }
        }


    }
}
