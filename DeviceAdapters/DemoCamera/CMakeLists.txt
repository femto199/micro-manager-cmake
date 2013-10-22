link_directories(${Boost_LIBRARY_DIRS})
include_directories(${Boost_INCLUDE_DIRS})
include_directories(${CMAKE_SOURCE_DIR}/MMDevice/include)

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

target_link_libraries(
    mmgr_dal_DemoCamera
    MMDevice
    ${Boost_LIBRARIES}
)
