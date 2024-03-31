import mysql.connector

def connect_to_database():
    try:
        # Establish a connection to the MySQL database
        connection = mysql.connector.connect(
            host="localhost",
            user="root",
            password="12345678",
            database="note_taking_db"
        )
        
        # Check if the connection is successful
        if connection.is_connected():
            print("Connected to the note-taking-db database")
        # Return the connection object
        return connection
    
    except Exception as error:
        print("Failed to connect to the note-taking-db database:", error)
        return None