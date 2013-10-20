find_package(SWIG REQUIRED)
include(${SWIG_USE_FILE})

find_package(Java REQUIRED)
include(UseJava)
find_package(JNI)

include_directories(${JDK_INCLUDE} ${JDK_PLATFORM_INCLUDE})
include_directories(${JNI_INCLUDE_DIRS})
include_directories(${CMAKE_SOURCE_DIR}/MMDevice/include)
include_directories(${CMAKE_SOURCE_DIR}/MMCore/include)

file(
    GLOB_RECURSE
    json_source_files
    ${CMAKE_SOURCE_DIR}/mmstudio/src/org/json/*.java
)

set_source_files_properties(MMCoreJ.i PROPERTIES CPLUSPLUS ON)
set(SWIG_MODULE_MMCoreJ_EXTRA_DEPS MMCore)
swig_add_module(MMCoreJ_wrap java MMCoreJ.i)
swig_link_libraries(MMCoreJ_wrap MMCore MMDevice)

add_jar(MMCoreJ mmcorej/*.java ${json_source_files})

# Installation directives (to use with make install)
install(TARGETS MMCoreJ_wrap
        DESTINATION ${IJ_PATH}/lib/micro-manager
        COMPONENT MMCoreJ)
install_jar(MMCoreJ ${IJ_PATH}/plugins/Micro-Manager
            COMPONENT MMCoreJ)
