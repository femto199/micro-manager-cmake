project(MMCORE)

link_directories(${Boost_LIBRARY_DIRS})
include_directories(${Boost_INCLUDE_DIRS})

include_directories(include)
include_directories(${Boost_INCLUDE_DIRS})

include_directories(${CMAKE_SOURCE_DIR}/MMDevice/include)

file(
    GLOB_RECURSE
    source_files
    src/*
    include/*
)

add_library(
    MMCore
    SHARED
    ${source_files}
)

target_link_libraries(
    MMCore
    MMDevice
    dl z
    ${Boost_LIBRARIES}
)

if(APPLE)
    find_library(FRAMEWORK_CoreFoundation CoreFoundation)
    find_library(FRAMEWORK_IOKit IOKit)
    target_link_libraries(
        MMCore
        ${FRAMEWORK_CoreFoundation}
        ${FRAMEWORK_IOKit}
    )
endif()

# This is mainly built to bundle with the JNI and Python bindings
add_library(
    MMCore_static
    STATIC
    ${source_files} )

target_link_libraries(
    MMCore_static
    MMDevice
    dl z
    ${Boost_LIBRARIES}
)

set_target_properties(MMCore_static PROPERTIES POSITION_INDEPENDENT_CODE TRUE )
