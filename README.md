
# Resume Builder

A full-stack Resume Builder web application — using a backend API + a modern frontend. Users can create their resumes, preview them and export/download in a structured format.

Live Demo:

Frontend: https://resumebuilderapi.netlify.app/

Backend API: https://resume-builder-backend-0tnv.onrender.com



## Table of Contents

🚀 About The Project

Features

Tech Stack

Getting Started (Local Setup)

    Backend Setup

    Frontend Setup

Usage

Deployment / Live Version

Folder Structure

Contributing

License

Contact / Author
## 🚀 About The Project

This project is a resume-builder web application composed of:

A backend REST API serving resume data (CRUD operations for user/resume data).

A frontend UI where users can input details, preview the resume, and generate/download it.

The idea is to provide an easy, web-based way for users to build resumes without requiring desktop software — all data can be stored in backend (or local storage) and resume exported quickly.
## Features

Create / Edit your resume data (personal info, education, experience, skills, etc.)

Preview resume in a formatted view

Export / Download resume 

Responsive UI (works on desktop / mobile)

Clean separation between frontend and backend (API-driven)
## Tech Stack

Backend: JavaSpring Boot, REST APIs, Spring Security, JWT, Cloudinary, Razorpay,
GitHub

Frontend: React, TypeScript, CSS/Styling 

APIs: REST API endpoints for data CRUD

Deployment: Backend deployed on Render, Frontend deployed on Netlify
## Screenshot
<img width="1919" height="924" alt="Screenshot 2025-12-07 230512" src="https://github.com/user-attachments/assets/45c0bbf1-9c23-43d7-a004-726c637650ab" />
<img width="1882" height="719" alt="Screenshot 2025-12-07 230527" src="https://github.com/user-attachments/assets/45d30fd0-0005-4cde-8c90-880da3e846f8" />
<img width="1896" height="812" alt="Screenshot 2025-12-07 230651" src="https://github.com/user-attachments/assets/fcb43af3-fd23-4579-afd5-4948233073e4" />
<img width="1880" height="744" alt="Screenshot 2025-12-07 230916" src="https://github.com/user-attachments/assets/b75ac35c-da8d-4027-943b-efc3a27f04b2" />




## Getting Started (Local Setup)

### Backend Setup
#### Clone the backend repo git clone https://github.com/RajatPandey10/Resume_Builder_Backend.git

#### Enter into the project folder 
***cd Resume_Builder_Backend***

#### Build and run the application (with Maven)
***mvn clean install
mvn spring-boot:run***

The backend should start at something like http://localhost:8080 (or configured port).

### Frontend Setup

#### Clone the frontend repo
git clone https://github.com/RajatPandey10/Resume_Builder_Frontend.git

#### Enter into the project folder
cd Resume_Builder_Frontend

#### Install dependencies
npm install

Create .env (or copy .env.example) and add any necessary environment variables (e.g. backend API base URL).

#### Run the application locally
npm start

#### Open browser at http://localhost:3000 (or port configured) to see the app.
## Folder Structure

### Backend
<img width="591" height="710" alt="Screenshot 2025-12-07 230245" src="https://github.com/user-attachments/assets/9a8c0b3c-6077-4cbd-8855-8d298f54062f" />


### Frontend
<img width="433" height="745" alt="Screenshot 2025-12-07 230010" src="https://github.com/user-attachments/assets/b571adbd-3d10-4aa3-9704-de11b217f814" />



## License

Specify the license under which you’re sharing the project.
E.g., MIT License — or whichever you prefer.
## Contact / Author

Rajat Pandey https://github.com/RajatPandey10 – creator/maintainer of this project
