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
set(IJ_PATH "../ImageJ-cmake" CACHE FILEPATH "Path to ImageJ folder. Where MM will be installed")

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

# add_subdirectory(DeviceAdapters)
# add_subdirectory(DeviceKit)

add_subdirectory(mmstudio)
add_subdirectory(plugins)
add_subdirectory(acqEngine)
# add_subdirectory(autofocus)

add_subdirectory(scripts)

# add_subdirectory(TestDeviceAdapters)
# add_subdirectory(Test_Programs)
# add_subdirectory(Tutorial)
# add_subdirectory(doc)


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
        DESTINATION ${IJ_PATH}
        COMPONENT demo_config)
install(FILES ${3RD_PARY_PUBLIC_PATH}/classext/bsh-2.0b4.jar
              ${3RD_PARY_PUBLIC_PATH}/classext/swingx-0.9.5.jar
        DESTINATION ${IJ_PATH}/plugins
        COMPONENT 3rd_party_jars)
