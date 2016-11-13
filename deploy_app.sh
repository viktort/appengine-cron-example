#!/bin/bash
echo "*** deploying project ***"

#  write out jar file versions and names to VERSIONS.txt file and save the
#  application name to an env var
app_name=`ls -1 target/*.war  | cut -d "/" -f 2 | tee VERSIONS.txt | grep -v original | tail -n 1 | cut -d "-" -f 1`

gcloud auth activate-service-account --key-file account.json

gcloud config set project $GCLOUD_PROJECT

gsutil ls -L gs://"${GSTORAGE_DEST_BUCKET}"

if [ $? -ne 0 ]; then

    echo gs://"${GSTORAGE_DEST_BUCKET}" bucket does not exist
    gsutil mb -l europe-west1 -p $GOOGLE_PROJECT_ID gs://"${GSTORAGE_DEST_BUCKET}"
    echo $?

fi

gsutil -m cp -r target/appengine-try-java-1.0/* gs://${GSTORAGE_DEST_BUCKET}/appengine-try-java-1.0/

ret=$?
if [ $ret -ne 0 ]; then
  echo "Failed to cp application files to gstorage"
  exit $ret
fi

curl -o $HOME/appengine-java-sdk-1.9.46.zip https://storage.googleapis.com/appengine-sdks/featured/appengine-java-sdk-1.9.46.zip
unzip -q -d $HOME $HOME/appengine-java-sdk-1.9.46.zip

cd $HOME/$CIRCLE_PROJECT_REPONAME && $HOME/appengine-java-sdk-1.9.46/bin/appcfg.sh -A $GCLOUD_PROJECT -V 2 --service_account_json_key_file=account.json update target/appengine-try-java-1.0

