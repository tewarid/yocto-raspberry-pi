# Raspberry Pi Linux system based on Yocto Project

To build using Docker, clone this repo and run

```bash
docker build .
```

To evaluate build output, create a container using `docker run -it image_id`. Use `docker image ls` to find image id. You can copy image files from the container to host using `docker cp`.
