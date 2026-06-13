# Tech Stack - Customer Support System

This document outlines the technical architecture and tools selected for the Customer Support Management System, based on the project plan.

## 🎨 Frontend
- **Framework**: [React](https://react.dev/) (via [Vite](https://vitejs.dev/)) - For a fast, modern, and reactive user interface.
- **Styling**: [Tailwind CSS](https://tailwindcss.com/) - For rapid UI development and a clean, responsive design.
- **State Management**: React Context API or Zustand (depending on complexity).
- **HTTP Client**: Axios - For communication with the Spring Boot backend.
- **Charts**: Chart.js or Recharts - To implement the dashboard metrics and distributions.

## ⚙️ Backend
- **Language/Framework**: [Java](https://www.oracle.com/java/) with [Spring Boot](https://spring.io/projects/spring-boot) - For a robust, scalable, and enterprise-grade REST API.
- **Security**: Spring Security - To handle admin authentication and authorization.
- **API Style**: REST - For seamless integration with the React frontend.
- **Build Tool**: Maven or Gradle.

## 🗄️ Database
- **Primary Database**: [PostgreSQL](https://www.postgresql.org/) - Relational database for storing tickets, users, internal notes, and conversation threads.
- **Vector Storage**: [pgvector](https://github.com/pgvector/pgvector) - An extension for PostgreSQL to store and query embeddings for the Knowledge Base (RAG), keeping the stack simple by avoiding a separate vector DB.

## 🤖 AI & Knowledge Base (RAG)
- **AI Orchestration**: [Spring AI](https://spring.io/projects/spring-ai) or [LangChain4j](https://langchain4j.github.io/langchain4j/) - To integrate LLMs for ticket categorization and auto-responses.
- **LLM**: OpenAI GPT-4o / GPT-4o-mini (via API) - For high-quality categorization and response generation.
- **Embeddings**: OpenAI `text-embedding-3-small` - To convert PDF content into vectors.
- **Document Processing**: Apache PDFBox or Tika - To extract text from the provided PDF knowledge base.

## ✉️ Email Infrastructure
- **Ingestion**: Webhook integration (e.g., via SendGrid Inbound Parse or Mailgun) - To receive customer emails and trigger ticket creation.
- **Outbound**: Spring Boot Starter Mail / JavaMail - To send responses back to customers.

## 🚀 Infrastructure & DevOps
- **Containerization**: Docker - For consistent environments across development and production.
- **Version Control**: Git.
- **Deployment**: AWS/Azure/GCP or a VPS (e.g., DigitalOcean) with a CI/CD pipeline.
