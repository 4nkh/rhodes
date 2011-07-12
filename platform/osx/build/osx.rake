namespace "config" do
  
  task :set_osx_platform do
    $current_platform = "osx" unless $current_platform
  end
  
  task :osx => [:set_osx_platform, "config:common"] do
    $qmake = "qmake"
    $make = "make"
    $macdeployqt = "macdeployqt"
    $name_tool = "install_name_tool"
    $move = "mv"
    $remove = "rm"
    $qt_project_dir = File.join( $startdir, 'platform/shared/qt/' )
    $build_dir = File.join( $startdir, 'platform/osx/bin/' )
  end
end

namespace "build" do
  namespace "osx" do
    task :rhosimulator => ["config:set_osx_platform", "config:osx"] do
        if not File.directory?($build_dir)
          Dir.mkdir($build_dir)
        end

        dirs = ['rubylib/', 'rholib/', 'sqlite3/', 'syncengine/', 'RhoSimulator/']
        dirs.each {|dir|
          dir_path = File.join( $build_dir, dir )
          if not File.directory?(dir_path)
            Dir.mkdir(dir_path)
          end
          tmp_path = File.join( dir_path, 'tmp/' )
          if not File.directory?(tmp_path)
            Dir.mkdir(tmp_path)
          end
        }
        gen_path = File.join( $build_dir, 'RhoSimulator/generated_files/' )
        if not File.directory?(gen_path)
          Dir.mkdir(gen_path)
        end

        app_path = File.join( $build_dir, 'RhoSimulator/RhoSimulator.app' )
        puts Jake.run($remove,['-R', app_path ])

        chdir $qt_project_dir
        args = ['-o', 'Makefile', '-r', '-spec', 'macx-g++', 'RhoSimulator.pro']
        puts Jake.run($qmake,args)
        args = ['clean']
        puts Jake.run($make,args)
        args = ['all']
        puts Jake.run($make,args)

        unless $? == 0
          puts "Error building"
          exit 1
        end

        args = [app_path]
        puts Jake.run($macdeployqt,args)

        exe_path = File.join( app_path, 'Contents/MacOS/RhoSimulator' )
        frm_path = File.join( app_path, 'Contents/Frameworks/' )
        fw_path = ['@executable_path/../Frameworks/', '.framework/Versions/Current', '.framework/Versions/4']
        libs = ['QtCore', 'QtGui', 'QtNetwork', 'QtWebKit']
        libs.each {|lib|
          args = [ frm_path + lib + fw_path[1], frm_path + lib + fw_path[2] ]
          puts Jake.run($move,args)
          args = [ '-change', fw_path[0] + lib + fw_path[1] + '/' + lib, fw_path[0] + lib + fw_path[2] + '/' + lib, exe_path]
          puts Jake.run($name_tool,args)
        }

        puts Jake.run($remove,['-R', File.join(frm_path, 'QtDeclarative.framework' )])
        puts Jake.run($remove,['-R', File.join(frm_path, 'QtOpenGL.framework' )])
        puts Jake.run($remove,['-R', File.join(frm_path, 'QtScript.framework' )])
        puts Jake.run($remove,['-R', File.join(frm_path, 'QtSql.framework' )])
        puts Jake.run($remove,['-R', File.join(frm_path, 'QtSvg.framework' )])
        puts Jake.run($remove,['-R', File.join(frm_path, 'QtXmlPatterns.framework' )])

        chdir $qt_project_dir
        args = ['clean']
        puts Jake.run($make,args)
    end
  end
end
