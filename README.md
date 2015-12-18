# Sclaav, An Audio/Visual Library In Scala

A library and application for manipulating and creating images and audio.

## Functionality:

Functionality which presently works well and default parameters are tuned to work well for most cases.

| What                         | Where                                            | Test |
| ---------------------------- |:------------------------------------------------:|:-----:|
| [Image mosaic](#mosaic)      | [DoMosaic](https://github.com/ogeagla/sclaav/blob/master/src/main/scala/com/oct/sclaav/visual/assembly/mosaic/DoMosaic.scala) |[DoesMosaicTest](https://github.com/ogeagla/sclaav/blob/master/src/test/scala/com/oct/sclaav/visual/assembly/mosaic/DoesMosaicTest.scala)|
| [Image similarity](#similarity) | [ImageMatchers](https://github.com/ogeagla/sclaav/tree/master/src/main/scala/com/oct/sclaav/visual/search/ImageMatchers.scala)|[ImageMatchersTest](https://github.com/ogeagla/sclaav/tree/master/src/test/scala/com/oct/sclaav/visual/search/ImageMatchersTest.scala)|

## Experimental Functionality:

Functionality which may or may not work well yet, where default parameters are a WIP, etc.

| What                         | Where                                            | Test |
| ---------------------------- |:------------------------------------------------:|:-----:|
| Genetic Algorithm for sub-image matching      | [SimpleCompleteGeneticAssembler](https://github.com/ogeagla/sclaav/blob/master/src/main/scala/com/oct/sclaav/visual/assembly/genetic/SimpleCompleteGeneticAssembler.scala) |[SimpleCompleteGeneticAssemblerTest](https://github.com/ogeagla/sclaav/blob/master/src/test/scala/com/oct/sclaav/visual/assembly/genetic/SimpleCompleteGeneticAssemblerTest.scala)|
| Image mosaic using non-uniform quadrilateral grid      | [QuadrilateralAssembler](https://github.com/ogeagla/sclaav/blob/master/src/main/scala/com/oct/sclaav/visual/assembly/grid/QuadrilateralAssembler.scala) |[QuadrilateralAssemblerTest](https://github.com/ogeagla/sclaav/blob/master/src/test/scala/com/oct/sclaav/visual/assembly/grid/QuadrilateralAssemblerTest.scala)|

## Building

```
$ ./sbt clean assembly
```

will create the far jar: `bin/Sclaav.jar`

## Running Tests

```
$ ./sbt clean test
```

## Using via CLI

This is the preferred method for using this tool.

### Mosaic

| Original | Mosaic |
| -------- | ------ |
| <img src='https://github.com/ogeagla/sclaav/blob/master/src/test/resources/below-average-photography/0207-2014-05-1808-12-27-IMG_1328_marked.jpg'  height="200" width="300"> | <img src ='https://github.com/ogeagla/sclaav/blob/master/src/test/resources/assembled/mosaic/boulder-foothills-mosaic.jpeg' height="200" width="300"> |

 - Given a `target` image (or set of target images) and a set of `sample` images,
    - Discretize target image in grid by `rows` and `cols`.
    - For each sub-image, find the `sample` image which is the closest match; by default we use min-`2-norm` distance between `ARGB` average vectors.
    - Assemble each matched sample image onto the original to create the mosaic.
    - Eyeballs be amazed.

Run with:

```
$ java -jar bin/Sclaav.jar \ # invokes jar
  --in file:///input-files-dir \     # input images directory
  --out file:///output-dir \         # output directory
  --mode permute \                   # permute or single mode
  --rows 64 \                        # how many rows to split target
  --cols 64 \                        # how many cols to split target
  --samples 300 \                    # how many samples to use from input dir
  --filters true \                   # apply and use filtered images
  --target file:///photo.jpg         # if mode is single, specify the target image to use
```

### Similarity

Given a target image, print out the images which best matches based on a distance metric and similarity measure (Euclidean ARGB for now).
 
Run with:

```
$ java -jar bin/Sclaav.jar \ 
  --in file:///input-files-dir \     
  --out file:///output-dir \         
  --mode similarity \            
  --samples 300 \            
  --target file:///photo.jpg         
```

## Docker

This comes with a Docker image!  Build with:
```
./sbt docker
```

The container contains:
 - The assembly jar in `/opt/sclaav/Sclaav.jar`
 - DCRAW installation.  Test with `/opt/dcraw/scripts/test.sh`.

To use the container as a DCRAW execution environment, first start the container, then run a shell into it.

Run the container with:
```
docker run oct/sclaav:latest
```
Check that it is running with:
```
docker ps
```
Find the container ID and then run:
```
docker exec -it <container-id> bash
```
You will then have a prompt inside the running container.  Run the test script with:
```
sh /opt/dcraw/scripts/test.sh
```
Which should create a thumbnail jpeg in `/opt/dcraw/samples`.

## Credits
http://www.octaviangeagla.com/bap

## License
Apache 2.0
