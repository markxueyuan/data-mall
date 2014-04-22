(ns language.usingGPU
  (:require calx))

(def src
  "//scale from -2.5 to 1
  float scale_x(float x){
  return (x / 1000.0) * 3.5 -2.5;
  }

  //scale from -1 to 1
  float scale_y(float y){
  return (y / 1000.0) * 2.0 -1.0;
  }

  __kernal void escape(__global float *out){
  int i = get_global_id(0);
  int j = get_global_id(1);
  int index = j * get_global_size(0) + i;
  float point_x = scale_x(i);
  float point_y = scale_y(j);
  int max_iterations =1000;
  int iteration = 0;
  float x = 0.0;
  float y = 0.0;

  while(x*x + y*y <= 4 && iteration < max_iterations){
  float tmp_x = (x*x - y*y) + point_x;
  y = (2 * x * y) + point_y;
  x = tmp_x;
  iteration++;
  }

  out[index] = iteration;
  }")
