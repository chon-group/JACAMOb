ARDUINO_CLI="/usr/bin/arduino-cli"
HERE="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

SKETCH="$HERE/firmware/"
echo $SKETCH
FQBN="arduino:avr:uno"

echo "========================================"
echo "       NECK - Compilation Test"
echo "========================================"
echo ""

#
# 1. Arduino CLI
#
if [[ ! -f "$ARDUINO_CLI" ]]
then
    echo "Arduino CLI not found."
    echo "Installing Arduino CLI..."
    echo ""
    echo "deb [trusted=yes] http://packages.chon.group/ chonos main" | sudo tee /etc/apt/sources.list.d/chonos.list

    sudo apt update
    sudo apt install -y arduino-cli
else
    echo "Arduino CLI already installed."
fi

echo ""

#
# 2. Arduino hardware
#
echo "Checking Arduino connection..."

ARDUINO_PORT=""
BOARD_LIST="$(arduino-cli board list)"

if echo "$BOARD_LIST" | grep -q "^/dev/ttyUSB0"
then
    ARDUINO_PORT="/dev/ttyUSB0"

elif echo "$BOARD_LIST" | grep -q "^/dev/ttyACM0"
then
    ARDUINO_PORT="/dev/ttyACM0"

else
    echo ""
    echo "========================================"
    echo "NECK TEST: FAIL"
    echo "========================================"
    echo "No Arduino was detected by arduino-cli."
    echo ""
    echo "Expected:"
    echo "  /dev/ttyUSB0"
    echo "or"
    echo "  /dev/ttyACM0"
    echo ""
    echo "$BOARD_LIST"
    echo ""
    exit 1
fi

echo "Arduino detected at $ARDUINO_PORT"
echo ""

#
# 3. Arduino AVR core
#
if ! arduino-cli core list | grep -q "^arduino:avr "
then
    echo "Arduino AVR core not found."
    echo "Updating Arduino indexes..."

    arduino-cli core update-index

    echo "Installing Arduino AVR core..."
    arduino-cli core install arduino:avr
else
    echo "Arduino AVR core already installed."
fi

echo ""

#
# 6. Compile
#
echo "========================================"
echo "Compiling NECK validation sketch..."
echo "Board: $FQBN"
echo "========================================"
echo ""

arduino-cli compile --fqbn "$FQBN" $SKETCH

RESULT=$?

echo ""

if [[ $RESULT -eq 0 ]]
then
    echo "========================================"
    echo "NECK COMPILATION TEST: PASS"
    echo "========================================"
else
    echo "========================================"
    echo "NECK COMPILATION TEST: FAIL"
    echo "========================================"
    exit 1
fi

#
# 7. Upload
#
echo ""
echo "========================================"
echo "Uploading NECK validation sketch..."
echo "Board: $FQBN"
echo "Port:  $ARDUINO_PORT"
echo "========================================"
echo ""

arduino-cli upload --port "$ARDUINO_PORT" --fqbn "$FQBN" $SKETCH

UPLOAD_RESULT=$?

echo ""

if [[ $UPLOAD_RESULT -eq 0 ]]
then
    echo "========================================"
    echo "NECK DEPLOY TEST: PASS"
    echo "========================================"
else
    echo "========================================"
    echo "NECK DEPLOY TEST: FAIL"
    echo "========================================"
    exit 1
fi
