#!/usr/bin/env python
"""
setup.py file for MM python binding
"""

from distutils.core import setup, Extension
import os

try:
    import numpy
    try:
        numpy_include = numpy.get_include()
    except AttributeError:
        numpy_include = numpy.get_numpy_include()

    # To use #include "arrayobject.h" or #include "numpy/arrayobject.h"
    numpy_include = [numpy_include, os.path.join(numpy_include, 'numpy')]
except:
    numpy_include = ""

os.environ['CC'] = 'g++'
#os.environ['CXX'] = 'g++'
#os.environ['CPP'] = 'g++'
#os.environ['LDSHARED'] = 'g++'

# numpy_include should be sufficient but just in case
# old path is also included
mac_numpy_inc_dir = "/Developer/SDKs/MacOSX10.5.sdk/System/Library/Frameworks/Python.framework/Versions/2.5/Extras/lib/python/numpy/core/include/numpy"

mmcorepy_module = Extension('_MMCorePy',
                            sources=['MMCorePy_wrap.cxx',
                                     '../MMDevice/DeviceUtils.cpp',
                                     '../MMDevice/ImgBuffer.cpp',
                                     '../MMDevice/Property.cpp',
                                     '../MMCore/CircularBuffer.cpp',
                                     '../MMCore/Configuration.cpp',
                                     '../MMCore/CoreCallback.cpp',
                                     '../MMCore/CoreProperty.cpp',
                                     '../MMCore/FastLogger.cpp',
                                     '../MMCore/MMCore.cpp',
                                     '../MMCore/PluginManager.cpp'],
                            language="c++",
                            extra_compile_args=['-fpermissive'],
                            extra_objects=[],
                            include_dirs=[mac_numpy_inc_dir] + numpy_include
                            )

setup(name='MMCorePy',
      version='0.1',
      author="MM Devs",
      description="Python binding for MM",
      ext_modules=[mmcorepy_module],
      py_modules=["MMCorePy"],
      )
