#!/bin/bash
set -e

HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

PYTHON_DIR="$HERE/python"
VENV="$PYTHON_DIR/pythonENV"
LIB_DIR="$PYTHON_DIR/lib"

JAVINO_URL="https://packages.chon.group/python/javino/"

prepare()
{
    echo "[Body] Preparing environment..."

    mkdir -p "$LIB_DIR"

    JAVINO_WHEEL=$(find "$LIB_DIR" -maxdepth 1 -name "javino*.whl" -print -quit)

    if [[ -z "$JAVINO_WHEEL" ]]
    then
        echo "[Body] Downloading Javino..."

        wget \
            --content-disposition \
            -P "$LIB_DIR" \
            "$JAVINO_URL"

        JAVINO_WHEEL=$(find "$LIB_DIR" -maxdepth 1 -name "javino*.whl" -print -quit)
    fi

    if [[ ! -f "$VENV/bin/activate" ]]
    then
        echo "[Body] Creating Python virtual environment..."
        rm -rf "$VENV"
        python3 -m venv "$VENV"
    fi

    source "$VENV/bin/activate"

    if ! python -c "import javino" >/dev/null 2>&1
    then
        echo "[Body] Installing Javino..."
        pip install "$JAVINO_WHEEL"
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

# #!/bin/bash
# set -e

# HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# PYTHON_DIR="$HERE/python"
# VENV="$PYTHON_DIR/pythonENV"

# echo "[Body] Preparing environment..."

# # Recreate an incomplete/broken virtual environment
# if [[ ! -f "$VENV/bin/activate" ]]
# then
#     echo "[Body] Creating Python virtual environment..."
#     rm -rf "$VENV"
#     python3 -m venv "$VENV"
# fi

# source "$VENV/bin/activate"

# if ! python -c "import javino" >/dev/null 2>&1
# then
#     echo "[Body] Installing Javino..."

#     TMP_DIR="$(mktemp -d)"

#     wget \
#         --content-disposition \
#         -P "$TMP_DIR" \
#         https://packages.chon.group/python/javino/

#     pip install "$TMP_DIR"/javino*.whl

#     rm -rf "$TMP_DIR"
# fi

# echo "[Body] Connecting Rosie's body..."

# exec python "$PYTHON_DIR/ConnectBody.py"