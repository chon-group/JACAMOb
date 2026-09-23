#!/bin/bash
set -e

HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

PYTHON_DIR="$HERE/python"
VENV="$PYTHON_DIR/pythonENV"


prepare()
{
    echo "[Body] Preparing environment..."

    if [[ ! -f "$VENV/bin/activate" ]]
    then
        echo "[Body] Creating Python virtual environment..."
        rm -rf "$VENV"
        python3 -m venv "$VENV"
    fi

    source "$VENV/bin/activate"

    if ! python -c "import serial" >/dev/null 2>&1
    then
        echo "[Body] Installing pyserial..."
        pip install pyserial
    fi
}


connect()
{
    source "$VENV/bin/activate"

    echo "[Body] Connecting Rosie's body..."
    exec python "$PYTHON_DIR/ConnectBody.py"
}


case "$1" in
    --prepare)
        prepare
        ;;

    --connect)
        connect
        ;;

    *)
        prepare
        connect
        ;;
esac