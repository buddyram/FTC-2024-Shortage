# RFrame Software Architecture & Design

## Overview

RFrame is a robotics framework built for FTC (FIRST Tech Challenge) that provides abstractions for drivetrain control, odometry, autonomous actions, and subsystem management. It separates hardware-agnostic logic from FTC-specific implementations, enabling reuse across robot designs and even simulation.

---

## High-Level System Architecture

```mermaid
graph TB
    subgraph Opmodes
        BO[BaseOpmode]
        TE[Teleop]
        AC[AutoClose15]
        AC2[AutoClose30099]
    end

    subgraph Robot
        NDB[NewDecodeBot]
    end

    subgraph Subsystems
        LA[Launcher]
        IN[Intake]
    end

    subgraph Drive
        HPDA[HolonomicPositionDriveAdapter]
        MDT[MecanumDriveTrain]
    end

    subgraph Odometry
        CO[CachedOdometry]
        GO[GroundingOdometry]
        OTOS[SparkFunOTOSOdometry]
        LL[LimelightOdometry]
    end

    subgraph Actions
        RA[RobotAction]
        DTA[DriveToAction]
        DTRA[DriveToAndRotateAction]
        MA[MultiAction]
    end

    TE --> BO
    AC --> BO
    AC2 --> BO
    BO --> NDB
    NDB --> LA
    NDB --> IN
    NDB --> HPDA
    NDB --> CO
    HPDA --> MDT
    HPDA --> CO
    CO --> GO
    GO --> OTOS
    GO --> LL
    RA --> NDB
    DTA --> RA
    DTRA --> RA
    MA --> RA
```

---

## Core Type Hierarchy

```mermaid
classDiagram
    class Robot {
        <<interface>>
        +getLogger() Logger
        +isActive() boolean
    }

    class Driveable~DT~ {
        <<interface>>
        +getDrive() DT
    }

    class Navigatable~DT~ {
        <<interface>>
        +getOdometry() Odometry~Pose3D~
    }

    class BaseComponent~R~ {
        #robot R
        +getRobot() R
    }

    Robot <|-- Driveable
    Driveable <|-- Navigatable
    Navigatable <|.. NewDecodeBot

    class NewDecodeBot {
        +launcher Launcher
        +intake Intake
        +odometry Odometry~Pose3D~
        +drive HolonomicDriveTrain
        +aimOn boolean
        +isRed boolean
        +autoAim() double
        +adjustFlywheelSpeed()
        +runAutoSequence(config)
    }
```

---

## Drive System

The drive system uses a layered adapter pattern. Raw motor commands are abstracted behind `DriveTrain`, and field-relative translation is added by wrapping the drivetrain with an odometry-aware adapter.

```mermaid
classDiagram
    class DriveTrain~I~ {
        <<interface>>
        +drive(instruction I)
    }

    class HolonomicDriveTrain {
        <<interface>>
    }

    class HolonomicDriveInstruction {
        +rotation double
        +speed double
        +direction double
    }

    class MecanumDriveTrain {
        -fl Motor
        -fr Motor
        -bl Motor
        -br Motor
        -powerLimit double
        +drive(HolonomicDriveInstruction)
    }

    class KiwiDriveTrain {
        -f Motor
        -bl Motor
        -br Motor
        +drive(HolonomicDriveInstruction)
    }

    class HolonomicPositionDriveAdapter {
        -driveTrain HolonomicDriveTrain
        -odometry Odometry~Pose3D~
        +drive(HolonomicDriveInstruction)
    }

    DriveTrain <|-- HolonomicDriveTrain
    HolonomicDriveTrain <|.. MecanumDriveTrain
    HolonomicDriveTrain <|.. KiwiDriveTrain
    HolonomicDriveTrain <|.. HolonomicPositionDriveAdapter
    HolonomicPositionDriveAdapter --> HolonomicDriveTrain : wraps
    HolonomicPositionDriveAdapter --> Odometry : reads heading
```

### Drive Instruction Flow

```mermaid
flowchart LR
    A["Field-relative command<br/>(direction, speed, rotation)"] --> B[HolonomicPositionDriveAdapter]
    B -->|"direction -= heading"| C[MecanumDriveTrain]
    C -->|"sin/cos decomposition"| D["FL / FR / BL / BR<br/>motor powers"]
```

---

## Odometry Stack

Odometry uses sensor fusion. The SparkFun OTOS provides continuous dead-reckoning, while the Limelight camera provides absolute position fixes via AprilTags. `GroundingOdometry` blends them, and `CachedOdometry` reduces polling overhead.

```mermaid
classDiagram
    class Odometry~P~ {
        <<interface>>
        +get() P
        +init() boolean
        +setPosition(P pos)
        +cleanup()
    }

    class SparkFunOTOSOdometry {
        -otos SparkFunOTOS
        +get() Pose3D
    }

    class LimelightOdometry {
        -limelight Limelight3A
        +get() Pose3D
        +getMT1() Pose3D
        +updateOrientation(yaw)
    }

    class GroundingOdometry~P~ {
        -absolute Odometry~P~
        -relative Odometry~P~
        -condition GroundingCondition
        +sync()
        +get() P
    }

    class CachedOdometry~T~ {
        -cache Cache~T~
        +refresh()
        +get() T
    }

    Odometry <|.. SparkFunOTOSOdometry
    Odometry <|.. LimelightOdometry
    Odometry <|.. GroundingOdometry
    Odometry <|.. CachedOdometry
    GroundingOdometry --> SparkFunOTOSOdometry : relative
    GroundingOdometry --> LimelightOdometry : absolute
    CachedOdometry --> GroundingOdometry : wraps
```

### Sensor Fusion Data Flow

```mermaid
flowchart TB
    OTOS["SparkFunOTOS<br/>(continuous, drifts)"] --> GO[GroundingOdometry]
    LL["Limelight 3A<br/>(intermittent, absolute)"] --> GO
    GO -->|"condition check"| DECIDE{Robot stationary?}
    DECIDE -->|yes| SYNC["Sync: copy absolute → relative"]
    DECIDE -->|no| PASS["Use relative"]
    SYNC --> CO[CachedOdometry<br/>TTL 100ms]
    PASS --> CO
    CO --> NDB[NewDecodeBot]
    CO --> HPDA[PositionDriveAdapter]
```

---

## Coordinate System

```mermaid
graph LR
    subgraph Pose3D
        direction TB
        P["position: Vector3D<br/>(x, y, z) inches"]
        R["rotation: Vector3D<br/>(roll, pitch, yaw) degrees"]
        PV["positionVelocity: Vector3D<br/>(dx, dy, dz) in/s"]
        RV["rotationVelocity: Vector3D<br/>(droll, dpitch, dyaw) deg/s"]
    end
```

```mermaid
flowchart LR
    subgraph "Field Coordinates (144 x 144 in)"
        BG["BLUE_GOAL<br/>(0, 137)"]
        RG["RED_GOAL<br/>(144, 137)"]
        O["Origin<br/>(0, 0)"]
    end
    O -.->|"x = 144 - x"| MIRROR["mirrorIfRed()"]
```

Blue and red field positions are mirrored across the x-axis midline (`x = 72`). All autonomous routines are authored in blue coordinates and mirrored at runtime via `BotUtilsNew.mirrorIfRed()`.

---

## Action System

Actions are composable units of autonomous behavior. They follow a strategy/decorator pattern where wrappers add timeout, conditional, and sequencing behavior.

```mermaid
classDiagram
    class RobotAction~R~ {
        <<interface>>
        +run(R drive) boolean
    }

    class MultiAction~R~ {
        -actions List~RobotAction~
        +run(R) boolean
    }

    class BaseConditionalAction~R~ {
        <<abstract>>
        -condition DriveCondition
        +execute(R)*
        +waitingForCompletion(R)*
        +run(R) boolean
    }

    class ConditionalWrapperAction~R~ {
        -action RobotAction
        -condition DriveCondition
    }

    class TimeoutWrapperAction~R~ {
        -action RobotAction
        -timeout long
    }

    class DriveToAction~T~ {
        -target Vector3D
        -accuracy double
        -speed CalculateDriveSpeed
    }

    class DriveToAndRotateAction~T~ {
        -target Vector3D
        -targetAngle double
        -accuracy double
    }

    class RotateToAction~T~ {
        -targetAngle double
        -accuracy double
    }

    RobotAction <|.. MultiAction
    RobotAction <|.. BaseConditionalAction
    RobotAction <|.. TimeoutWrapperAction
    RobotAction <|.. DriveToAction
    RobotAction <|.. DriveToAndRotateAction
    RobotAction <|.. RotateToAction
    BaseConditionalAction <|-- ConditionalWrapperAction
    MultiAction o-- RobotAction : contains
    TimeoutWrapperAction o-- RobotAction : wraps
    ConditionalWrapperAction o-- RobotAction : wraps
```

### Action Composition Example

```mermaid
flowchart TB
    MS[MultiAction] --> A1[DriveToAndRotateAction<br/>move to shoot position]
    MS --> A2[ConditionalWrapperAction<br/>wait for turret ready]
    MS --> A3[TimeoutWrapperAction<br/>shoot for 1300ms]
    MS --> A4[DriveToAndRotateAction<br/>move to intake position]
```

---

## Launcher Subsystem

```mermaid
classDiagram
    class Launcher {
        +wheel Flywheel
        +turret Turret
        +hood Hood
        +blocker Blocker
    }

    class Flywheel {
        -motor RPMMotor
        -targetRPM double
        +setRPM(rpm)
        +getRPM() double
        +isReady() boolean
    }

    class Turret {
        -motor DcMotorEx
        +setAngle(degrees)
        +isReady() boolean
        -powerForAngleDelta(delta) double
    }

    class Hood {
        -servo Servo
        +setAngle(position)
    }

    class Blocker {
        -servo Servo
        +OPEN double
        +CLOSED double
        +setAngle(position)
    }

    class BaseComponent~R~ {
        #robot R
    }

    BaseComponent <|-- Launcher
    BaseComponent <|-- Flywheel
    BaseComponent <|-- Turret
    BaseComponent <|-- Hood
    BaseComponent <|-- Blocker
    Launcher *-- Flywheel
    Launcher *-- Turret
    Launcher *-- Hood
    Launcher *-- Blocker
```

### Turret Power Curve

The turret uses linear interpolation to scale motor power based on how far it needs to rotate, preventing timing belt slip on large movements.

```mermaid
xychart-beta
    title "Turret Power vs Angle Delta"
    x-axis "Angle Delta (degrees)" [0, 30, 60, 90, 120, 150, 180]
    y-axis "Motor Power" 0.2 --> 0.7
    line [0.60, 0.55, 0.50, 0.45, 0.40, 0.35, 0.30]
```

### Auto-Aim Flow

```mermaid
flowchart TB
    POS["Odometry position"] --> CALC["atan2(goal - pos)"]
    HEAD["Odometry heading"] --> CALC
    CALC --> ANGLE["Raw turret angle"]
    OFF["turretOffset<br/>(manual trim)"] --> ANGLE
    ANGLE --> OVR{overrideAngle set?}
    OVR -->|yes| USE_OVR[Use override]
    OVR -->|no| USE_CALC[Use calculated]
    USE_OVR --> AIM_CHECK{aimOn?}
    USE_CALC --> AIM_CHECK
    AIM_CHECK -->|no| ZERO["angle = 0<br/>(center turret)"]
    AIM_CHECK -->|yes| SET["turret.setAngle(angle)"]
    ZERO --> SET
```

---

## Intake Subsystem

```mermaid
classDiagram
    class Intake {
        +sweeper Sweeper
        +enableMode(Modes mode)
    }

    class Sweeper {
        -motor DcMotor
        +intaking()
        +outtaking()
        +idle()
    }

    class Modes {
        <<enum>>
        INTAKING
        IDLE
        OUTTAKING
    }

    BaseComponent <|-- Intake
    BaseComponent <|-- Sweeper
    Intake *-- Sweeper
    Intake --> Modes
```

---

## Opmode Lifecycle

```mermaid
sequenceDiagram
    participant FTC as FTC Runtime
    participant BO as BaseOpmode
    participant BG as Background Thread
    participant EX as execute()

    FTC->>BO: runOpMode()
    BO->>BO: initializeHardware()
    Note over BO: Prompt alliance color<br/>Init sensors, motors, servos<br/>Build odometry stack

    BO->>BG: start (MIN_PRIORITY)
    BO->>FTC: waitForStart()
    FTC-->>BO: START pressed

    par Background Loop
        loop while !interrupted
            BG->>BG: cachedOdometry.refresh()
            BG->>BG: updateGlobals()
            BG->>BG: controlIntake()
            BG->>BG: autoAim()
            BG->>BG: adjustFlywheelSpeed()
        end
    and Main Thread
        BO->>EX: execute()
        Note over EX: Teleop loop or<br/>Auto sequence
    end

    EX-->>BO: return / exception
    BO->>BG: interrupt()
    BO->>BG: join()
    BO->>BO: cleanup (reset turret, odometry)
```

---

## Autonomous Sequence System

Autonomous routines are configured declaratively using `AutoSequenceConfig` and the `AutoStep` enum, then executed by `runAutoSequence()`.

```mermaid
stateDiagram-v2
    [*] --> PreloadTurret: Start auto
    PreloadTurret --> NextStep: Aim at shoot position

    state NextStep <<choice>>
    NextStep --> SHOOT: step == SHOOT
    NextStep --> NEAR: step == NEAR
    NextStep --> MIDDLE: step == MIDDLE
    NextStep --> FAR: step == FAR
    NextStep --> OPEN_GATE: step == OPEN_GATE
    NextStep --> INTAKE_GATE: step == INTAKE_GATE

    SHOOT --> DriveToShootPos
    DriveToShootPos --> WaitTurret: Switch to live aim
    WaitTurret --> Fire: Unblock + intake
    Fire --> PreloadNext: Re-preload turret
    PreloadNext --> NextStep: Next step

    NEAR --> IntakeClose
    MIDDLE --> IntakeMiddle
    FAR --> IntakeFar
    OPEN_GATE --> PushGate
    INTAKE_GATE --> DriveToGate

    IntakeClose --> NextStep
    IntakeMiddle --> NextStep
    IntakeFar --> NextStep
    PushGate --> NextStep
    DriveToGate --> NextStep

    NextStep --> Park: No more steps
    Park --> [*]
```

### Example: AutoClose15 Sequence

```mermaid
flowchart LR
    S1[SHOOT] --> M[MIDDLE] --> S2[SHOOT] --> IG[INTAKE_GATE] --> S3[SHOOT] --> N[NEAR] --> S4[SHOOT] --> F[FAR] --> S5[SHOOT] --> P[PARK]

    style S1 fill:#e74c3c,color:#fff
    style S2 fill:#e74c3c,color:#fff
    style S3 fill:#e74c3c,color:#fff
    style S4 fill:#e74c3c,color:#fff
    style S5 fill:#e74c3c,color:#fff
    style M fill:#3498db,color:#fff
    style N fill:#3498db,color:#fff
    style F fill:#3498db,color:#fff
    style IG fill:#2ecc71,color:#fff
    style P fill:#95a5a6,color:#fff
```

---

## Flywheel Speed Control

The flywheel RPM is calculated as an exponential function of distance to goal, allowing harder shots from farther away. The hood angle adjusts in discrete bands to change the launch trajectory.

```mermaid
flowchart TB
    DIST["distance to goal"] --> RPM["RPM = 2350.5 * 1.00529^dist + speed offset"]
    DIST --> HOOD{distance range}
    HOOD -->|"> 78 in"| H1["hood = 0.55<br/>(flat)"]
    HOOD -->|"> 68 in"| H2["hood = 0.50"]
    HOOD -->|"<= 68 in"| H3["hood = 0.96<br/>(steep)"]
    OVR{overrideHood set?} -->|yes| H4["hood = overrideHood"]
    H1 --> OVR
    H2 --> OVR
    H3 --> OVR
    RPM --> FW["Flywheel.setRPM()"]
    OVR -->|no| DONE[Apply]
    H4 --> DONE
```

---

## Hardware Map

```mermaid
graph TB
    subgraph Motors
        DFL["dfl - Drive Front Left"]
        DFR["dfr - Drive Front Right"]
        DBL["dbl - Drive Back Left"]
        DBR["dbr - Drive Back Right"]
        LFLY["LFly - Flywheel (DcMotorEx)"]
        TUR["turret - Turret (DcMotorEx)"]
        INT["intake - Intake"]
    end

    subgraph Servos
        BLOCK["blocker - Gate"]
        HOOD["hood - Launch Angle"]
    end

    subgraph Sensors
        OTOS_HW["otos - SparkFun OTOS"]
        LL_HW["limelight - Limelight 3A"]
    end

    subgraph Drivetrain
        DFL --> MDT[MecanumDriveTrain]
        DFR --> MDT
        DBL --> MDT
        DBR --> MDT
    end

    subgraph Launcher System
        LFLY --> FW[Flywheel]
        TUR --> TU[Turret]
        HOOD --> HO[Hood]
        BLOCK --> BL[Blocker]
        FW --> LAUNCH[Launcher]
        TU --> LAUNCH
        HO --> LAUNCH
        BL --> LAUNCH
    end

    OTOS_HW --> OTOS_O[SparkFunOTOSOdometry]
    LL_HW --> LL_O[LimelightOdometry]
```

---

## Key Design Patterns

| Pattern | Where | Purpose |
|---|---|---|
| **Adapter** | HolonomicPositionDriveAdapter | Converts field-relative to robot-relative |
| **Decorator** | TimeoutWrapperAction, ConditionalWrapperAction | Adds behavior to actions |
| **Composite** | MultiAction | Sequences actions |
| **Strategy** | CalculateDriveSpeed, CalculateRotationSpeed | Pluggable velocity profiles |
| **Template Method** | BaseOpmode.runOpMode → execute() | Consistent init with custom logic |
| **Sensor Fusion** | GroundingOdometry | Blends absolute + relative sensors |
| **Caching** | CachedOdometry, Cache\<T\> | Reduces sensor polling overhead |
| **Factory** | BotUtilsNew | Convenient action construction |
| **Component** | BaseComponent\<R\> | Uniform subsystem structure |
