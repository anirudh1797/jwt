To Do

1. Update application properties with mysql db credentials
2. After starting the application, the following generated tables will be available in mysql

'refreshtoken'
'roles'
'user_roles'
'users'

API and usage

User API - Creates user with username, password, email and role
1. Create Student 
2. Create Admin
3. Create Professor
4. Create user with two roles

Token API - Generates Access token and Refresh token
5. Auth Token
6. Refresh Token

Test API
7. Common API -  All users
8. Student API - Only for student
9. Professor API - Only for Professor
10. Admin API - Only for Admin

Tested with Java 17 and MySQL

java version "17.0.1" 2021-10-19 LTS
Java(TM) SE Runtime Environment (build 17.0.1+12-LTS-39)
Java HotSpot(TM) 64-Bit Server VM (build 17.0.1+12-LTS-39, mixed mode, sharing)

Please find the postman JSON collection with project