# A Photomosaic Generator In Scala

## Using

```
$ ./sbt clean assembly
$ java -jar bin/Mosaical.jar \
  --in input-files-dir \
  --out output-dir \
  --mode permute \
  --rows 64 \ 
  --cols 64 \
  --maxSamplePhotos 300 \
  --manipulate true \
  --target photo.jpg
```