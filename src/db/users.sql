-- name: add-user!
INSERT INTO Users(Username, Email, Password) VALUES (:username, :email, :password) ;

-- name: update-user-email!
UPDATE Users SET Email = :email WHERE Username = :username ;

-- name: update-user-password!
UPDATE Users SET Password = :password WHERE Username = :username ;

-- name: get-user
SELECT Username,Email,Password FROM Users WHERE Username = :username ;

-- name: get-users
SELECT Username,Email,Password FROM Users ;

-- name: delete-user!
DELETE FROM Users WHERE Username = :username ;