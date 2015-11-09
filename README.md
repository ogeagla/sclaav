# Sclaav, An Audio/Visual Library In Scala

## Using Visual Functions

### Mosaic

```
$ ./sbt clean assembly
$ java -jar bin/Sclaav.jar \
  --in input-files-dir \
  --out output-dir \
  --mode permute \
  --rows 64 \ 
  --cols 64 \
  --samples 300 \
  --filters true \
  --target photo.jpg
```

#### Overview

 - Given a `target` image (or set of target images) and a set of `sample` images,
    - Discretize target image in grid by `rows` and `cols`.
    - For each sub-image, find the `sample` image which is the closest match; by default we use min-`2-norm` distance between `ARGB` average vectors.
    - Assemble each matched sample image onto the original to create the mosaic.
    - Eyeballs be amazed.
