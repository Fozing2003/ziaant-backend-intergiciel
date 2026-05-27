param(
    [switch]$Build
)

$ErrorActionPreference = "Stop"
$ProjectName = "ziaant-backend"

if ($Build) {
    docker compose -p $ProjectName up -d --build
} else {
    docker compose -p $ProjectName up -d
}

docker compose -p $ProjectName ps
