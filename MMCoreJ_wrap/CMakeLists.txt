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

file(
    GLOB_RECURSE
    tagged_image_file
    ${CMAKE_CURRENT_SOURCE_DIR}/TaggedImage.java
)

set(CMAKE_SWIG_OUTDIR ${CMAKE_CURRENT_BINARY_DIR}/mmcorej)

set_source_files_properties(MMCoreJ.i PROPERTIES CPLUSPLUS ON)
set(SWIG_MODULE_MMCoreJ_wrap_EXTRA_DEPS MMCore_static)
swig_add_module(MMCoreJ_wrap java MMCoreJ.i)
swig_link_libraries(MMCoreJ_wrap MMCore_static MMDevice ${JNI_LIBRARIES} ${JAVA_AWT_LIBRARY} ${JAVA_JVM_LIBRARY})

set(CMAKE_JAVA_JAR_ENTRY_POINT ${CMAKE_CURRENT_BINARY_DIR})
add_jar(MMCoreJ ${CMAKE_SWIG_OUTDIR}/*.java
        ${json_source_files} ${tagged_image_file})
add_dependencies(MMCoreJ MMCoreJ_wrap)

# Installation directives (to use with make install)
install(TARGETS MMCoreJ_wrap
        DESTINATION ${IJ_PATH}/lib/micro-manager
        COMPONENT MMCoreJ)
install_jar(MMCoreJ ${IJ_PATH}/plugins/Micro-Manager
            COMPONENT MMCoreJ)
