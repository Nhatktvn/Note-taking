class User:
    def __init__(self, email, username, password, isPremium):
        self.email = email
        self.username = username
        self.password = password
        self.isPremium = isPremium

    def __str__(self):
        return f"User(email={self.email}, username={self.username}, password={self.password}, isPremium={self.isPremium})"
