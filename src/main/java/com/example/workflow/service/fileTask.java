package com.example.workflow.service;

/*@Service
public class fileTask implements JavaDelegate {
    @Autowired
    private CoefficientViewMapper CoefficientViewMapper;
    @Autowired
    private EmployeePositionMapper EmployeePositionMapper;
    //@Autowired
    //private WageScoreMapper WageScoreMapper;
    //@Autowired
    //private WagePieceMapper WagePieceMapper;
    @Autowired
    private EmpKpiMapper EmpKpiMapper;
    @Autowired
    private EmpWageService EmpWageService;
    @Autowired
    private EmpRewardMapper EmpRewardMapper;

    /*@Override
    public void execute(DelegateExecution execution) throws Exception {
        /*Long positionId=new Long(1);
        System.out.println(execution.getVariable("JavaClass"));
        System.out.println("DelegateExpressionServiceTask");

        LambdaQueryWrapper<CoefficientView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(CoefficientView::getPositionId,positionId);
        CoefficientView coefficient=CoefficientViewMapper.selectOne(queryWrapper);

        LambdaQueryWrapper<EmployeePosition> Wrapper=new LambdaQueryWrapper<>();
        Wrapper.eq(EmployeePosition::getPositionId,positionId);
        List<EmployeePosition> empList=EmployeePositionMapper.selectList(Wrapper);

        empList.forEach(x->{
            Wage wage=new Wage();
            wage.setBaseWage(coefficient.getBaseWage()
                    .multiply(BigDecimal.valueOf(coefficient.getPositionCoefficient()))
                    .multiply(BigDecimal.valueOf(coefficient.getRegionCoefficient())));

            LambdaQueryWrapper<WageScore> Wrapper1=new LambdaQueryWrapper<>();
            Wrapper1.eq(WageScore::getEmpId,x.getEmpId());
            List<WageScore> scoreList=WageScoreMapper.selectList(Wrapper1);
            BigDecimal scoreTotal=new BigDecimal(0);
            if(!scoreList.isEmpty()) {
                scoreList.forEach(y -> {
                    BigDecimal score = y.getExamine()
                            .multiply(BigDecimal.valueOf(y.getScorePercent()))
                            .multiply(BigDecimal.valueOf(y.getAssessorPercent()));
                    scoreTotal.add(score);
                });
            }
            wage.setScoreWage(scoreTotal);

            LambdaQueryWrapper<WagePiece> Wrapper2=new LambdaQueryWrapper<>();
            Wrapper2.eq(WagePiece::getEmpId,x.getEmpId());
            Wrapper2.eq(WagePiece::getExamine,1);
            List<WagePiece> pieceList=WagePieceMapper.selectList(Wrapper2);
            BigDecimal pieceTotal=new BigDecimal(0);
            if(!pieceList.isEmpty()){
                pieceList.forEach(y->{
                    BigDecimal score=y.getTargetNum();
                    pieceTotal.add(score);
                });
            }
            wage.setPieceWage(pieceTotal);

            LambdaQueryWrapper<EmpKpi> Wrapper3=new LambdaQueryWrapper<>();
            Wrapper3.eq(EmpKpi::getEmpId,x.getEmpId());
            List<EmpKpi> kpiList=EmpKpiMapper.selectList(Wrapper3);
            BigDecimal kpiTotal=new BigDecimal(0);
            if(!kpiList.isEmpty()){
                kpiList.forEach(y->{
                    BigDecimal kpi=y.getResult();
                    kpiTotal.add(kpi);
                });
            }
            wage.setPieceWage(kpiTotal);

            LambdaQueryWrapper<EmpReward> wrapper4=new LambdaQueryWrapper<>();
            wrapper4.eq(EmpReward::getEmpId,x.getEmpId());
            List<EmpReward> rewardList=EmpRewardMapper.selectList(wrapper4);
            BigDecimal rewardTotal=new BigDecimal(0);
            if(!rewardList.isEmpty()){
                rewardList.forEach(y->{
                    BigDecimal reward=y.getReward();
                    rewardTotal.add(reward);
                });
            }
            wage.setRewardWage(rewardTotal);

            WageService.save(wage);
        });
    }

}*/
