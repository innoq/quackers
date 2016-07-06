-- name: add-user!
INSERT INTO Users(Username, Email, Password) VALUES (:username, :email, :password) ;

-- name: update-user!
UPDATE Users SET Email = :email SET Password = :password WHERE Username = :username ;

-- name: get-user
SELECT Username,Email,Password FROM Users WHERE Username = :username ;

-- name: get-users
SELECT Username,Email,Password FROM Users ;

-- name: delete-user!
DELETE FROM Users WHERE Username = :username ;