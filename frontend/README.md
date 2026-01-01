# Personal Issue Tracker - Frontend

## Description
Frontend for the Personal Issue Tracker developed with React 18 and TypeScript.

## Technologies
- **Framework**: React 18 with TypeScript
- **Build Tool**: Vite
- **Styling**: Tailwind CSS
- **State Management**: React Query (TanStack Query)
- **Forms**: React Hook Form + Zod validation
- **Routing**: React Router DOM
- **Testing**: Vitest + Testing Library

## Project Structure
```
frontend/
├── src/
│   ├── components/      # Reusable components
│   ├── pages/          # Page components
│   ├── services/       # API services
│   ├── types/          # TypeScript definitions
│   ├── utils/          # Utilities and helpers
│   ├── context/        # React contexts
│   └── App.tsx         # Main application
├── public/             # Static assets
├── package.json        # Dependencies and scripts
└── vite.config.ts      # Vite configuration
```

## Main Commands

### Development
```bash
# Install dependencies
npm install

# Start development server
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview
```

### Testing
```bash
# Run tests
npm run test

# Run tests with UI
npm run test:ui

# Run tests once
npm run test:run

# Generate coverage report
npm run test:coverage
```

### Code Quality
```bash
# Lint code
npm run lint

# Fix linting issues
npm run lint:fix

# Format code
npm run format

# Check formatting
npm run format:check
```

## Configuration
- Environment variables in `.env` (see `.env.example`)
- Vite configuration in `vite.config.ts`
- TypeScript configuration in `tsconfig.json`
- Tailwind configuration in `tailwind.config.js`

## API Integration
The frontend integrates with the backend API through:
- Axios for HTTP requests
- React Query for state management
- TypeScript interfaces matching backend DTOs

## Language Versions

- **English**: [README.en.md](README.en.md)
- **Español**: [README.md](README.md)