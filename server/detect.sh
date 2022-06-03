#!/bin/bash
python detect.py --source ./data/images/ --weights ./weights/16species963.pt
#用16species963.pt去推理./data/images/下的所有图片