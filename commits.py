import os, time

def commit():
    while True:
        os.system("git add .")
        os.system("git commit -m \"Working on features\"")
        os.system("git push origin main")
        time.sleep(120)
        
commit()