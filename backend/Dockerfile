# Build stage using .NET 8 SDK
FROM mcr.microsoft.com/dotnet/sdk:8.0 AS build
WORKDIR /src

# Copy solution and project files
COPY ["AccountingApp.sln", "./"]
COPY ["src/Accounting.Domain/Accounting.Domain.csproj", "src/Accounting.Domain/"]
COPY ["src/Accounting.Application/Accounting.Application.csproj", "src/Accounting.Application/"]
COPY ["src/Accounting.Infrastructure/Accounting.Infrastructure.csproj", "src/Accounting.Infrastructure/"]
COPY ["src/Accounting.Api/Accounting.Api.csproj", "src/Accounting.Api/"]

# Restore dependencies
RUN dotnet restore "src/Accounting.Api/Accounting.Api.csproj"

# Copy full source
COPY src/ src/

# Build and publish
WORKDIR "/src/src/Accounting.Api"
RUN dotnet publish "Accounting.Api.csproj" -c Release -o /app/publish /p:UseAppHost=false

# Runtime stage using ASP.NET Core 8 Runtime
FROM mcr.microsoft.com/dotnet/aspnet:8.0 AS final
WORKDIR /app
EXPOSE 8080
COPY --from=build /app/publish .

ENV ASPNETCORE_URLS=http://+:8080
ENV ASPNETCORE_ENVIRONMENT=Development

ENTRYPOINT ["dotnet", "Accounting.Api.dll"]
