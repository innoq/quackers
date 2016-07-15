-- name: quack!
INSERT INTO Quacks(UserId, Quack) VALUES (:userid, :quack) ;

-- name: get-quacks
SELECT QuackID, Username, Quack, CreatedTS 
FROM Quacks, Users 
WHERE Quacks.UserID = Users.UserID 
ORDER BY CreatedTS DESC;

-- name: get-quacks-for-user
SELECT QuackID, Username, Quack, CreatedTS 
FROM Quacks, Users 
WHERE Quacks.UserID = Users.UserID 
  AND Users.Username = :username 
ORDER BY CreatedTS DESC;