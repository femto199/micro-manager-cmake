# TODO
# - better resource files: http://public.kitware.com/Bug/view.php?id=14500
#     - StageControl (icons)
#     - mmstudio (icons and plugins.config)

cmake_minimum_required(VERSION 2.8)
project(Micro-Manager)

set(APPLICATION_NAME "Micro-Manager")

set(APPLICATION_VERSION_MAJOR "1")
set(APPLICATION_VERSION_MINOR "4")
set(APPLICATION_VERSION_PATCH "16")
set(APPLICATION_VERSION "${APPLICATION_VERSION_MAJOR}.${APPLICATION_VERSION_MINOR}.${APPLICATION_VERSION_PATCH}")

set(CMAKE_MODULE_PATH "${CMAKE_SOURCE_DIR}/CMakeModules")

# Required external paths
set(3RD_PARY_PUBLIC_PATH "../3rdpartypublic" CACHE FILEPATH "Path to 3rdpartypublic folder")
set(IJ_PATH "../ImageJ" CACHE FILEPATH "Path to ImageJ folder. Where MM will be installed")

# Convert relative to absolute paths
get_filename_component(3RD_PARY_PUBLIC_PATH ${3RD_PARY_PUBLIC_PATH} ABSOLUTE)
get_filename_component(IJ_PATH ${IJ_PATH} ABSOLUTE)

set(Boost_USE_STATIC_LIBS OFF)
set(Boost_USE_MULTITHREADED ON)
set(Boost_USE_STATIC_RUNTIME OFF)
find_package(Boost REQUIRED COMPONENTS date_time exception system thread)
find_package(ZLIB REQUIRED)

add_subdirectory(MMDevice)
add_subdirectory(MMCore)

# Bindings
option(USE_SWIG_BINDINGS "Java and Python bindings" ON)
if(USE_SWIG_BINDINGS)
  find_package(SWIG REQUIRED)
  add_subdirectory(MMCoreJ_wrap)
  add_subdirectory(MMCorePy_wrap)
endif()

add_subdirectory(DeviceAdapters)

add_subdirectory(mmstudio)
add_subdirectory(acqEngine)
add_subdirectory(autofocus)
add_subdirectory(plugins)

add_subdirectory(scripts)

# add_subdirectory(TestDeviceAdapters)
# add_subdirectory(Test_Programs)
# add_subdirectory(Tutorial)
# add_subdirectory(doc)
# add_subdirectory(DeviceKit)


message(STATUS "**********************************")
message(STATUS "** Micro-Manager build options **")
message(STATUS "**********************************")

message(STATUS "* MM will be installed in ImageJ path: ${IJ_PATH}")
message(STATUS "* ImageJ path can be set with cmake -DIJ_PATH=/path/to/ImageJ ..")
message(STATUS "* 3rdpartypublic path: ${3RD_PARY_PUBLIC_PATH}")
message(STATUS "* 3rdpartypublic path can be set with cmake -D3RD_PARY_PUBLIC_PATH=/path/to/3rdpartypublic ..")

message(STATUS "**********************************")

# Installation directives (to use with make install)
install(FILES ${CMAKE_SOURCE_DIR}/bindist/any-platform/MMConfig_demo.cfg
        DESTINATION ./
        COMPONENT demo_config)

install(FILES ${3RD_PARY_PUBLIC_PATH}/classext/AbsoluteLayout.jar
              ${3RD_PARY_PUBLIC_PATH}/classext/bsh-2.0b4.jar
              ${3RD_PARY_PUBLIC_PATH}/classext/clojure.jar
              ${3RD_PARY_PUBLIC_PATH}/classext/clooj.jar
              ${3RD_PARY_PUBLIC_PATH}/classext/commons-math-2.0.jar
              ${3RD_PARY_PUBLIC_PATH}/classext/core.cache.jar
              ${3RD_PARY_PUBLIC_PATH}/classext/core.memoize.jar
              ${3RD_PARY_PUBLIC_PATH}/classext/data.json.jar
              ${3RD_PARY_PUBLIC_PATH}/classext/gproto.jar
              ${3RD_PARY_PUBLIC_PATH}/classext/JARVersionsAndSources.txt
              ${3RD_PARY_PUBLIC_PATH}/classext/jcommon-1.0.16.jar
              ${3RD_PARY_PUBLIC_PATH}/classext/jfreechart-1.0.13.jar
              ${3RD_PARY_PUBLIC_PATH}/classext/loci-common.jar
              ${3RD_PARY_PUBLIC_PATH}/classext/lwm.jar
              ${3RD_PARY_PUBLIC_PATH}/classext/ome-xml.jar
              ${3RD_PARY_PUBLIC_PATH}/classext/scifio.jar
              ${3RD_PARY_PUBLIC_PATH}/classext/slf4j-api-1.7.1.jar
              ${3RD_PARY_PUBLIC_PATH}/classext/substance.jar
              ${3RD_PARY_PUBLIC_PATH}/classext/swing-layout-1.0.4.jar
              ${3RD_PARY_PUBLIC_PATH}/classext/swingx-0.9.5.jar
              ${3RD_PARY_PUBLIC_PATH}/classext/trident.jar
              ${3RD_PARY_PUBLIC_PATH}/classext/TSFProto.jar
        DESTINATION ./plugins
        COMPONENT 3rd_party_jars)

# Install startup scripts
if(UNIX)
    if(APPLE)
        # Add OS X startupt script here
    else()
        install(PROGRAMS startup_scripts/mm_start_linux.sh
                DESTINATION ./
                COMPONENT startup_scripts)
        endif()
    endif()
else()
    # Add Windows startupt script here
endif()

# Instal ImageJ files
# TODO: add condition to not installer IJ, if IJ_PATH == CMAKE_INSTALL_PREFIX
install(DIRECTORY ${IJ_PATH}/
        DESTINATION ./)

# Installer generation

set(CPACK_PACKAGE_NAME "Micro-Manager")
set(CPACK_PACKAGE_VENDOR "www.micro-manager.org/")
set(CPACK_PACKAGE_DESCRIPTION_SUMMARY "µManager is software for control of microscopes. ")
set(CPACK_PACKAGE_VERSION ${APPLICATION_VERSION})
set(CPACK_PACKAGE_VERSION_MAJOR ${APPLICATION_VERSION_MAJOR})
set(CPACK_PACKAGE_VERSION_MINOR ${APPLICATION_VERSION_MINOR})
set(CPACK_PACKAGE_VERSION_PATCH ${APPLICATION_VERSION_PATCH})
set(CPACK_PACKAGE_INSTALL_DIRECTORY "Micro-Manager")
set(CPACK_PACKAGE_ICON "")

# NSIS related variable
set(CPACK_NSIS_MUI_ICON "")
set(CPACK_NSIS_MUI_UNIICON "")

# Debian related variable
set(CPACK_DEBIAN_PACKAGE_MAINTAINER "www.micro-manager.org")
set(CPACK_PACKAGE_CONTACT "www.micro-manager.org")
set(CPACK_DEBIAN_ARCHITECTURE ${CMAKE_SYSTEM_PROCESSOR})
set(CPACK_DEBIAN_PACKAGE_NAME "micro-manager")
set(CPACK_DEBIAN_PACKAGE_DEPENDS "") # TODO

# Add components to installer
set(CPACK_COMPONENTS_ALL MMCoreJ MMCorePy mmstudio mmautofocus
                         device_adaptaters acqEngine scripts
                         startup_scripts 3rd_party_jars demo_config mmplugins)

# Set components description
# TODO
set(CPACK_COMPONENT_MMCOREJ_DISPLAY_NAME "Java wrapper for MMCore lib")
set(CPACK_COMPONENT_MMCOREPY_DISPLAY_NAME "Python wrapper for MMCore lib")
set(CPACK_COMPONENT_MMSTUDIO_DISPLAY_NAME "ImageJ plugin: Micro Manager application")
set(CPACK_COMPONENT_MMAUTOFOCUS_DISPLAY_NAME "Autofocus algorithms")
set(CPACK_COMPONENT_DEVICE_ADAPTATERS_DISPLAY_NAME "Device adaptaters")
set(CPACK_COMPONENT_ACQENGINE_DISPLAY_NAME "Acquisition engine")
set(CPACK_COMPONENT_SCRIPTS_DISPLAY_NAME "Scripts")
set(CPACK_COMPONENT_STARTUP_SCRIPT_DISPLAY_NAME "Startup scripts")
set(CPACK_COMPONENT_3RD_PARTY_JARS_DISPLAY_NAME "Java libraries")
set(CPACK_COMPONENT_DEMO_CONFIG_DISPLAY_NAME "Demo configuration")
set(CPACK_COMPONENT_MMPLUGINS_DISPLAY_NAME "Micro-Manager plugins")

# Set components dependencies
# TODO
 set(CPACK_COMPONENT_MMSTUDIO_DEPENDS MMCoreJ)

# Set components in group
# TODO
set(CPACK_COMPONENT_MMSTUDIO_GROUP "Runtime")
set(CPACK_COMPONENT_MMCOREJ_GROUP "Runtime")
set(CPACK_COMPONENT_MMCOREPY_GROUP "Runtime")
set(CPACK_COMPONENT_STARTUP_SCRIPT_GROUP "Runtime")

# Add condition here to build NSIS installer in windows
if(UNIX)
    if(APPLE)
        set(CPACK_GENERATOR "BUNDLE")
    else()
        set(CPACK_GENERATOR "TGZ")
        if("${CPACK_GENERATOR}" STREQUAL "DEB")
            set(CPACK_PACKAGING_INSTALL_PREFIX "/usr/local/micro-manager")
        endif()
    endif()
else()
    set(CPACK_GENERATOR "NSIS")
endif()


include(CPack)

