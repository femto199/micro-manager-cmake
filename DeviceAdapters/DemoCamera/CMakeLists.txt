link_directories(${Boost_LIBRARY_DIRS})
include_directories(${Boost_INCLUDE_DIRS})
include_directories(${CMAKE_SOURCE_DIR}/MMDevice/include)
include_directories(${CMAKE_SOURCE_DIR}/MMCore/include)

file(
    GLOB_RECURSE
    source_files
    DemoCamera.cpp
    DemoCamera.h
    WriteCompactTiffRGB.h
)

add_library(
    mmgr_dal_DemoCamera
    SHARED
    ${source_files}
)

# see MMCore/src/PluginManager.cpp, line 51 how lib suffix is handled

target_link_libraries(
    mmgr_dal_DemoCamera
    MMDevice
    MMCore
    ${Boost_LIBRARIES}
)

install(TARGETS mmgr_dal_DemoCamera
        DESTINATION ${IJ_PATH}/lib/micro-manager
        COMPONENT device_adaptaters)
