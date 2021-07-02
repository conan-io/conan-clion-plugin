project(cmake-build-run-platform-test)

include(${CMAKE_BINARY_DIR}/conanbuildinfo.cmake)
conan_basic_setup()

add_executable(test_exe main.cpp)
target_link_libraries(test_exe ${CONAN_LIBS})
