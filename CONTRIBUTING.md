## Branching strategy:

This project uses a feature-based branching model with a dedicated develop branch for integration and a main branch for stable releases.

**`main`** – The stable, demo-ready branch. Only code that has been tested and approved on `develop` is merged here.  
- **`develop`** – The integration branch where all new features are merged and tested together before release.  
- **Feature branches** – Short-lived branches created from `develop` for specific tasks or components.  
  - `feat/backend/<feature>` for backend functionality  
  - `feat/frontend/<feature>` for JavaFX UI work  
  - `feat/db/<feature>` for database design (or Flyway scripts)  
  - `feat/db-int/<feature>` for database integration and repository logic  
- **Fix branches** – Used for small bug fixes (`fix/<issue>`)

### Workflow

1. Create a new feature branch from `develop`.  
2. Implement and test locally using Maven.  
3. Merge the completed branch back into `develop`.  
4. When all features are working together, merge `develop` into `main` and tag the release (e.g., `v1.0.0`).  

This approach keeps the `main` branch stable for demonstrations while allowing parallel development and easy integration on `develop`.
