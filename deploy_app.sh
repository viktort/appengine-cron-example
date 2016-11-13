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

curl -o $HOME/google_appengine_1.9.40.zip https://storage.googleapis.com/appengine-sdks/featured/google_appengine_1.9.40.zip
unzip -q -d $HOME $HOME/google_appengine_1.9.40.zip

cd $HOME/$CIRCLE_PROJECT_REPONAME && gcloud --quiet preview app deploy app.yaml --version 1 --promote
