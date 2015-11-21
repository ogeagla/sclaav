# Sclaav, An Audio/Visual Library In Scala

## Current Functionality:

| What                         | Where                                            | Test |
| ---------------------------- |:------------------------------------------------:|:-----:|
| [Image mosaic](#mosaic)      | `DoMosaic` |`DoesMosaicTest`|

## Building

```
$ ./sbt clean assembly
```

will create the far jar: `bin/Sclaav.jar`

## Running Tests

```
$ ./sbt clean test
```

## Using Visual Functions

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
  --in input-files-dir \     # input images directory
  --out output-dir \         # output directory
  --mode permute \           # permute or single mode
  --rows 64 \                # how many rows to split target
  --cols 64 \                # how many cols to split target
  --samples 300 \            # how many samples to use from input dir
  --filters true \           # apply and use filtered images
  --target photo.jpg         # if mode is single, specify the target image to use
```

## Experimental Functionality (not hooked up via CLI):

| What                         | Where                                            | Test |
| ---------------------------- |:------------------------------------------------:|:-----:|
| SimpleCompleteGeneticAssembler      | `SimpleCompleteGeneticAssembler` |`SimpleCompleteGeneticAssemblerTest`|
| QuadrilateralAssembler      | `QuadrilateralAssembler` |`QuadrilateralAssemblerTest`|

## Credits
http://www.octaviangeagla.com/bap

## License
Apache 2.0
