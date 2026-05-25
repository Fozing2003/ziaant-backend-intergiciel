$ErrorActionPreference = "Stop"
$ProjectName = "ziaant-backend"

docker compose -p $ProjectName stop
docker compose -p $ProjectName ps
