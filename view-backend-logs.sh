#!/bin/bash

SSH_USER="user1"
SSH_HOST="api.drivebit.my"
CONTAINER_NAME="drivebit-drivebitbackend-1"

if [ -n "$SSH_KEY" ]; then
    SSH_KEY="$SSH_KEY"
elif [ -f "$(dirname "$0")/id_rsa_api_drivebit" ]; then
    SSH_KEY="$(dirname "$0")/id_rsa_api_drivebit"
elif [ -f "$(dirname "$0")/id_rsa" ]; then
    SSH_KEY="$(dirname "$0")/id_rsa"
elif [ -f "$HOME/.ssh/id_rsa_api_drivebit" ]; then
    SSH_KEY="$HOME/.ssh/id_rsa_api_drivebit"
elif [ -f "$HOME/.ssh/id_rsa" ]; then
    SSH_KEY="$HOME/.ssh/id_rsa"
else
    echo "Error: SSH key not found. Checked:"
    echo "  - $(dirname "$0")/id_rsa_api_drivebit"
    echo "  - $(dirname "$0")/id_rsa"
    echo "  - $HOME/.ssh/id_rsa_api_drivebit"
    echo "  - $HOME/.ssh/id_rsa"
    echo "  - SSH_KEY environment variable"
    exit 1
fi

echo "Using SSH key: $SSH_KEY" >&2
chmod 600 "$SSH_KEY"

if [ "$1" == "-f" ] || [ "$1" == "--follow" ]; then
    ssh -o StrictHostKeyChecking=no -i "$SSH_KEY" "$SSH_USER@$SSH_HOST" "docker logs -f $CONTAINER_NAME" | grep "Sent"
elif [ "$1" == "-n" ] || [ "$1" == "--lines" ]; then
    LINES=${2:-100}
    ssh -o StrictHostKeyChecking=no -i "$SSH_KEY" "$SSH_USER@$SSH_HOST" "docker logs --tail $LINES $CONTAINER_NAME" | grep "Sent"
else
    ssh -o StrictHostKeyChecking=no -i "$SSH_KEY" "$SSH_USER@$SSH_HOST" "docker logs --tail 100 $CONTAINER_NAME" | grep "Sent"
fi


