DATA_SRC_URI='https://s3.amazonaws.com/nyc-tlc/trip+data/yellow_tripdata_2017-01.csv'
LOCAL_DATA_DEST='./data/taxi_data.csv'

echo 'Getting taxi data'
wget -O $LOCAL_DATA_DEST $DATA_SRC_URI

echo '🎉 Done 🎉'




