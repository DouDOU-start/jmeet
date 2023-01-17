#!/bin/bash

docker run -itd --name jmeet --net=host doudou/jmeet-meet:v1.0.0

docker exec -it jmeet /bin/bash