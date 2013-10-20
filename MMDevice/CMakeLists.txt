project(MMDevice)

include_directories(include)
file(
    GLOB_RECURSE
    source_files
    src/*
    include/*
)

add_library(
    MMDevice
    STATIC
    ${source_files}
)

set_target_properties( MMDevice PROPERTIES POSITION_INDEPENDENT_CODE TRUE )
