# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview
Customer Support Management System featuring a React frontend and a Spring Boot backend, integrating RAG (Retrieval-Augmented Generation) using PostgreSQL with pgvector and OpenAI.

## Tech Stack
- **Frontend**: React 19, Vite, Tailwind CSS 4
- **Backend**: Java 21, Spring Boot, Spring Security
- **Database**: PostgreSQL with `pgvector` extension
- **AI**: OpenAI GPT-4o / GPT-4o-mini, `text-embedding-3-small`

## Development Commands

### Backend
- **Run Server**: `cd backend && ./mvnw spring-boot:run`
- **Health Check**: `curl http://localhost:8080/api/health`
- **Build**: `cd backend && ./mvnw clean install`

### Frontend
- **Run Development Server**: `cd frontend && npm run dev`
- **Install Dependencies**: `cd frontend && npm install`
- **Build**: `cd frontend && npm run build`

## Architecture & Implementation Details

### High-Level Structure
- `/backend`: Spring Boot project containing the REST API, Security configuration, and Database entities.
- `/frontend`: React application using Vite and Tailwind CSS for the admin/monitoring dashboard.

### Key Technical Notes
- **Tailwind CSS v4**: The project uses Tailwind v4. Use `@import "tailwindcss";` in CSS files and the `@tailwindcss/postcss` plugin in `postcss.config.js`.
- **Security**: Current configuration in `SecurityConfig.java` allows all requests (`permitAll()`) and has CORS enabled for `http://localhost:5176` (and others) to facilitate frontend-backend communication during development.
- **Database**: PostgreSQL is used for both relational data and vector embeddings (via `pgvector`).
- **RAG Pipeline**: Designed to use OpenAI embeddings and LLMs for ticket categorization and auto-responses.
