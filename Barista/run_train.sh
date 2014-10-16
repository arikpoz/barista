#!/usr/bin/env sh

# setup required environment variables
export PATH=/opt/intel/vtune_amplifier_xe_2013/bin64:/opt/intel/bin/:/usr/local/cuda-6.0/bin:$PATH
export LD_LIBRARY_PATH=/opt/intel/composerxe/lib/intel64:/opt/intel/composerxe/mkl/lib/intel64:/opt/intel/mkl/lib/intel64:/usr/local/cuda-6.0/lib64:/usr/local/cuda-6.0/lib:/usr/local/lib:/opt/OpenBLAS/lib:$LD_LIBRARY_PATH

# $1 is caffe folder
# $2 is the full path to the solver file
$1/build/tools/train_net.bin $2

