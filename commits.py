import os, time

jovia = {
    "email": "joviajoyce7@gmail.com",
    "name": "viyyyyaaa",
}
myles = {
    "email": "mylesmark753@gmail.com",
    "name": "MulindwaMarkMyles"
}

timothy = {
    "email": "timothykalyango@gmail.com",
    "name": "Kalyangotimothy"
}

maurice = {
    "email": "nickbaraka96@gmail.com",
    "name" : "BarakaM10"
}

def set_git_config(user):
    os.system(f"git config user.email {user['email']}")
    os.system(f"git config user.name {user['name']}")
    os.system("git config user.email")
    os.system("git config user.name")
    
def print_choices():
    print("1. Jovia")
    print("2. Myles")
    print("3. Timothy")
    print("4. Maurice")
    print("5. Exit")
    
def get_choice():
    return int(input("Enter your choice: "))

def commit():
    while True:
        os.system("git add .")
        os.system("git commit -m \"Working on features\"")
        os.system("git push origin main")
        time.sleep(120)

def main():
    print_choices()
    choice = get_choice()
    if choice == 1:
        set_git_config(jovia)
    elif choice == 2:
        set_git_config(myles)
    elif choice == 3:
        set_git_config(timothy)
    elif choice == 4:
        set_git_config(maurice)
    else:
        print("Invalid choice")
            
    commit()    
    
main()