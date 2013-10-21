
find_package( SWIG REQUIRED )
include(${SWIG_USE_FILE})

set(Python_ADDITIONAL_VERSIONS "2.7")
find_package(PythonInterp 2.7 REQUIRED)
find_package(PythonLibs 2.7 REQUIRED)
find_package(Numpy)

include_directories(${PYTHON_INCLUDE_DIRS})
include_directories(${NUMPY_INCLUDE_DIRS})
include_directories(${CMAKE_SOURCE_DIR}/MMDevice/include)
include_directories(${CMAKE_SOURCE_DIR}/MMCore/include)

if(${CMAKE_SYSTEM_NAME} STREQUAL "Linux")
    set (CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fpermissive")
endif()

set_source_files_properties(MMCorePy.i PROPERTIES CPLUSPLUS ON)
set(SWIG_MODULE_MMCorePy_EXTRA_DEPS MMCore_static MMDevice)
swig_add_module(MMCorePy python MMCorePy.i)
swig_link_libraries(MMCorePy MMCore_static MMDevice ${PYTHON_LIBRARIES})

if(APPLE)
  swig_link_libraries(MMCorePy -Wl,-undefined,dynamic_lookup)
endif()

# Installation directives (to use with make install)
install(FILES ${CMAKE_BINARY_DIR}/MMCorePy_wrap/MMCorePy.py
        DESTINATION ${IJ_PATH}/lib/micro-manager
        COMPONENT MMCorePy)
install(TARGETS _MMCorePy
        DESTINATION ${IJ_PATH}/lib/micro-manager
        COMPONENT MMCorePy)
