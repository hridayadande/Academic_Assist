# Academic Assist

### _Comprehensive Student Support and Question Management Platform_

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![JavaFX](https://img.shields.io/badge/JavaFX-17+-blue.svg)](https://openjfx.io/)
[![H2 Database](https://img.shields.io/badge/Database-H2-green.svg)](http://www.h2database.com/)
[![JUnit](https://img.shields.io/badge/Testing-JUnit%205-yellowgreen.svg)](https://junit.org/junit5/)

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Installation & Setup](#installation--setup)
- [Usage Guide](#usage-guide)
- [User Roles & Permissions](#user-roles--permissions)
- [Database Schema](#database-schema)
- [Testing](#testing)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)
- [License](#license)

## 🎯 Overview

**Academic Assist** is a comprehensive student support platform designed specifically for academic institutions. Built for a typical course environment, it facilitates seamless communication between students, instructors, reviewers, and administrative staff through an intuitive question-and-answer system.

The platform provides role-based access control, ensuring that each user type (Student, Instructor, Staff, Admin, Reviewer) has appropriate permissions and functionalities tailored to their needs.

### 🌟 Key Highlights

- **Multi-role user management** with granular permissions
- **Real-time question and answer platform**
- **Advanced search and filtering capabilities**
- **Admin access control with approval workflows**
- **Comprehensive reviewer system with ratings**
- **Database-driven architecture** with robust data persistence
- **Modern JavaFX-based graphical user interface**

## ✨ Features

### 🎓 Student Features

- **Ask Questions**: Submit questions and get help from instructors and reviewers
- **Search & Filter**: Advanced search capabilities with multiple filter options
- **Reviewer Selection**: Browse and select reviewers based on expertise and ratings
- **Real-time Chat**: Direct communication with reviewers
- **Answer Tracking**: Track question status and new responses
- **Review System**: Rate and provide feedback for reviewers

### 👨‍🏫 Instructor Features

- **Question Management**: View and respond to student questions
- **Reviewer Scoring**: Assign scores to reviewers based on performance
- **User Administration**: Limited admin capabilities for user management
- **Admin Access Requests**: Request elevated admin privileges
- **Student Progress Tracking**: Monitor student engagement and activity

### 🔧 Staff Features

- **Question Support**: Assist students with their questions
- **User Management**: Basic user administration capabilities
- **Admin Requests**: Request admin access for additional privileges
- **System Monitoring**: Track platform usage and performance

### 👨‍💼 Admin Features

- **Complete User Management**: Create, modify, and delete user accounts
- **Role Assignment**: Assign and modify user roles
- **Access Control**: Approve or deny admin access requests
- **System Configuration**: Manage platform settings and configurations
- **Invitation System**: Generate invitation codes for new users
- **Reporting**: Comprehensive system reports and analytics

### 🔍 Reviewer Features

- **Profile Management**: Create detailed profiles with experience and background
- **Question Response**: Provide expert answers to student questions
- **Rating System**: Receive and view ratings from students
- **Review History**: Access history of all provided reviews
- **Specialized Support**: Offer domain-specific assistance

## 🛠 Technology Stack

**Java** | 17+ | Core application development  
**JavaFX** | 17+ | Modern GUI framework  
**H2 Database** | Latest | Embedded SQL database  
**JUnit 5** | Latest | Unit testing framework  
**Eclipse IDE** | 2023+ | Recommended development environment

## 📋 Prerequisites

Before setting up Academic Assist, ensure you have the following installed:

- **Java Development Kit (JDK) 17 or higher**
- **Eclipse IDE** (2023-06 or later recommended)
- **JavaFX SDK** (compatible with your Java version)
- **Git** (for cloning the repository)

## 🚀 Installation & Setup

### Step 1: Clone the Repository

```bash
git clone https://github.com/hridayadande/Team_Project_Repo.git
```

### Step 2: Import Project into Eclipse

1. Open Eclipse IDE
2. Navigate to `File → Import → Existing Projects into Workspace`
3. Select the cloned repository folder
4. Click `Finish`

### Step 3: Configure Build Path

1. Right-click on the project → `Properties`
2. Go to `Java Build Path → Modulepath`
3. Add the following libraries:
   - **JRE System Library**
   - **JavaFX SDK** (add external JARs from JavaFX lib folder)
   - **JUnit 5** (Add Library → JUnit → JUnit 5)

### Step 4: Configure Classpath

1. In `Java Build Path → Classpath`
2. Ensure only **H2 Database** is present in Classpath
3. Remove any other libraries from Classpath if present

### Step 5: Clean and Build Project

1. Navigate to `Project → Clean`
2. Select the project and click `Clean`
3. Wait for automatic rebuild to complete

### Step 6: Configure Run Configuration

1. Go to `Run → Run Configurations`
2. Create new Java Application configuration
3. Set Main class: `application.StartCSE360`
4. **For macOS users**: In VM arguments, uncheck "Use the -XstartOnFirstThread argument when launching with SWT"

### Step 7: Database Initialization (First Run Only)

1. Open `src/databasePart1/DatabaseHelper.java`
2. Locate line 43: `//statement.execute("DROP ALL OBJECTS");`
3. Uncomment this line (remove the //)
4. Save the file and run the application once
5. Close the application
6. Re-comment the line (add // back)
7. Save the file

### Step 8: Run the Application

1. Right-click on `src/application/StartCSE360.java`
2. Select `Run As → Java Application`
3. The application should launch with the welcome screen

## 📖 Usage Guide

### First Time Setup

1. **Initial Admin Setup**: The first user will be prompted to create an admin account
2. **Account Creation**: Follow the setup wizard to create your admin credentials
3. **User Invitation**: Use the admin panel to generate invitation codes for other users

### Logging In

1. Launch the application
2. Select your role (Student/Instructor/Staff/Admin/Reviewer)
3. Enter your credentials
4. Access your role-specific dashboard

### Student Workflow

1. **Ask Questions**: Use the "Ask Question" button to submit queries
2. **Browse Questions**: View all questions with filtering options
3. **Search**: Use the search bar to find specific topics
4. **Get Help**: Select reviewers and engage in direct chat
5. **Rate Reviewers**: Provide feedback after receiving help

### Admin Workflow

1. **Manage Users**: View, edit, and delete user accounts
2. **Generate Invites**: Create invitation codes for new users
3. **Handle Requests**: Approve or deny admin access requests
4. **System Monitoring**: Review platform usage and activities

## 👥 User Roles & Permissions

**Student** | Ask questions, search content, chat with reviewers, rate reviewers

**Instructor** | Answer questions, score reviewers, limited admin functions, request admin access

**Staff** | Support students, basic user management, request admin access

**Admin** | Full system control, user management, access control, system configuration

**Reviewer** | Answer questions, manage profile, view ratings and feedback

**Restricted** | Limited access (applied to problematic accounts)

## 🗄 Database Schema

The application uses H2 embedded database with the following main tables:

- **cse360users**: User account information and roles
- **InvitationCodes**: System-generated invitation codes
- **ReviewerRequests**: Reviewer access requests
- **admin_access_requests**: Admin privilege requests
- **Questions**: Student questions and metadata
- **Answers**: Responses to questions
- **ReviewerProfiles**: Reviewer background information
- **Reviews**: Feedback and ratings for reviewers

## 🧪 Testing

### Running Unit Tests

1. Navigate to `src/Jtesting/Jtest4.java`
2. Right-click → `Run As → JUnit Test`
3. **Important**: Ensure database reset step is completed before running tests

### Test Coverage

The test suite covers 12 comprehensive user stories including:

- Student question search functionality
- Reviewer search and selection
- Admin access control workflows
- Review and rating systems
- Request management and reopening

### Test Categories

- **User Authentication Tests**
- **Question Management Tests**
- **Reviewer System Tests**
- **Admin Access Control Tests**
- **Database Integration Tests**

## ❗ Troubleshooting

### Common Issues and Solutions

#### Database Connection Issues

```
Error: Database locked or connection failed
Solution: Ensure no other instances are running, restart application
```

#### JavaFX Runtime Issues

```
Error: JavaFX runtime components are missing
Solution: Verify JavaFX is properly added to module path, not classpath
```

#### Module Path Configuration

```
Error: Module not found
Solution: Check that all required modules are in module path:
- JRE System Library
- JavaFX SDK
- JUnit 5
```

#### macOS Specific Issues

```
Error: SWT threading issues
Solution: Uncheck "Use the -XstartOnFirstThread argument" in run configuration
```

## 🤝 Contributing

We welcome contributions to Academic Assist! Please follow these guidelines:

### Development Workflow

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Code Standards

- Follow Java naming conventions
- Add JavaDoc comments for public methods
- Include unit tests for new features
- Maintain backward compatibility

### Reporting Issues

Please use the GitHub issue tracker to report bugs or request features. Include:

- Detailed description of the issue
- Steps to reproduce
- Expected vs actual behavior
- System information (OS, Java version, etc.)

## 📄 License

This project is developed as part of CSE coursework at Arizona State University.

## 📞 Support

For support and questions:

- **Repository**: [GitHub Repository](https://github.com/hridayadande/Team_Project_Repo)

---

**Academic Assist** - _Empowering Academic Success Through Collaborative Learning_ 🎓✨
