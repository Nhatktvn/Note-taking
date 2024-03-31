class User:
    def __init__(self, id, phone, username, password, isPremium):
        self.id = id
        self.phone = phone
        self.username = username
        self.password = password
        self.isPremium = isPremium

    def __str__(self):
        return f"User(id={self.id}, phone={self.phone}, username={self.username}, password={self.password}, isPremium={self.isPremium})"