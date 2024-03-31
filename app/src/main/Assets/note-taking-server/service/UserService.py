from db_connect import connect_to_database
from model.User import User

class UserService:
    def __init__(self):
        self.db_connection = connect_to_database()

    def create_user(self, user: User):
        cursor = self.db_connection.cursor()
        query = "INSERT INTO users (email, username, password, isPremium) VALUES (%s, %s, %s, %s)"
        cursor.execute(query, (user.email, user.username, user.password, user.isPremium))
        self.db_connection.commit()
        cursor.close()
        return True

    def get_all_users(self):
        cursor = self.db_connection.cursor()
        query = "SELECT * FROM users"
        cursor.execute(query)
        users = [User(*row) for row in cursor.fetchall()]
        cursor.close()
        return users

    def get_user_by_email(self, email: str):
        cursor = self.db_connection.cursor()
        query = "SELECT * FROM users WHERE email = %s"
        cursor.execute(query, (email,))
        target = cursor.fetchone()
        cursor.close()
        if target is None:
            return None
        return User(*target)

    def user_login(self, email: str, password: str):
        user = self.get_user_by_email(email)
        if user is None:
            return None
        if user.password == password:
            return user
        return None

    def user_register(self, email: str, username: str, password: str, isPremium: bool):
        if self.get_user_by_email(email) is not None:
            return None
        user = User(email, username, password, isPremium)
        self.create_user(user)
        return user

    def update_user(self, user: User):
        cursor = self.db_connection.cursor()
        query = "UPDATE users SET username = %s, password = %s, isPremium = %s WHERE email = %s"
        cursor.execute(query, (user.username, user.password, user.isPremium, user.email))
        self.db_connection.commit()
        cursor.close()
        return True

    def delete_user(self, user: User):
        cursor = self.db_connection.cursor()
        query = "DELETE FROM users WHERE email = %s"
        cursor.execute(query, (user.email,))
        self.db_connection.commit()
        cursor.close()
        return True