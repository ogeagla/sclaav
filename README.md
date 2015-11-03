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
  --samples 300 \
  --filters true \
  --target photo.jpg
```