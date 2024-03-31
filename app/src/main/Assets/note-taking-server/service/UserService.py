from db_connect import connect_to_database
from model.User import User

class UserService:
    def __init__(self):
        self.db_connection = connect_to_database()
        pass

    def create_user(self, user:User):
        # Implement logic to create a new user
        cursor = self.db_connection.cursor()
        query = "INSERT INTO users (username, phone, password, isPremium) VALUES (%s, %s, %s, %s)"
        cursor.execute(query, (user.username, user.phone, user.password, user.isPremium))
        self.db_connection.commit()
        cursor.close()
        return True
    def get_user_by_id(self, user_id:int):
        # Implement logic to get a user by ID
        cursor = self.db_connection.cursor()
        query = "SELECT * FROM users WHERE id = %s"
        cursor.execute(query, (user_id,))
        user = cursor.fetchone()
        cursor.close()
        return user
    def get_all_users(self):
        # Implement logic to get all users
        cursor = self.db_connection.cursor()
        query = "SELECT * FROM users"
        cursor.execute(query)
        users = cursor.fetchall()
        cursor.close()
        return users
    def get_user_by_phone(self, phone:str):
        # Implement logic to get a user by phone
        cursor = self.db_connection.cursor()
        query = "SELECT * FROM users WHERE phone = %s"
        cursor.execute(query, (phone,))
        target = cursor.fetchone()
        if target is None:
            cursor.close()
            return None
        user = User(target[0], target[1], target[2], target[3], target[4])
        cursor.close()
        return user
    def user_login(self, phone:str, password:str):
        # Implement logic to login a user
        user = self.get_user_by_phone(phone)
        if user is None:
            return None
        if user.password.__eq__(password):
            return user
        return None

    def update_user(self, user:User):
        # Implement logic to update a user by ID
        cursor = self.db_connection.cursor()
        query = "UPDATE users SET username = %s, phone = %s, password =%s, isPremium = %s WHERE id = %s"
        cursor.execute(query, (user.username, user.phone, user.password, user.isPremium, user.id))
        self.db_connection.commit()
        cursor.close()
        return True

    def delete_user(self, user_id:int):
        # Implement logic to delete a user by ID
        cursor = self.db_connection.cursor()
        query = "DELETE FROM users WHERE id = %s"
        cursor.execute(query, (user_id,))
        self.db_connection.commit()
        cursor.close()
        return True
    def delete_user(self, user:User):
        # Implement logic to delete a user by ID
        cursor = self.db_connection.cursor()
        query = "DELETE FROM users WHERE id = %s"
        cursor.execute(query, (user.id))
        self.db_connection.commit()
        cursor.close()
        return True