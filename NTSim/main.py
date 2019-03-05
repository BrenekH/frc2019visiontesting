from networktables import NetworkTables
import time

if __name__ == "__main__":
    NetworkTables.initialize()
    while True:
        time.sleep(1)
        