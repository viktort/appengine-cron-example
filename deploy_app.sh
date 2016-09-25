#!/bin/bash
echo "*** deploying project ***"

#  write out jar file versions and names to VERSIONS.txt file and save the
#  application name to an env var
app_name=`ls -1 target/*.jar  | cut -d "/" -f 2 | tee VERSIONS.txt | grep -v original | tail -n 1 | cut -d "-" -f 1`

/opt/google-cloud-sdk/bin/gcloud auth activate-service-account --key-file account.json

gsutil cp -r target/source-1.0-ALPHA/* gs://${GSTORAGE_DEST_BUCKET}/source-1.0-ALPHA/
ret=$?
if [ $ret -ne 0 ]; then
  echo "Failed to cp application files to gstorage"
  exit $ret
fi
