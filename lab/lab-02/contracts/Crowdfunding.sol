pragma solidity ^0.4.24;

import "./Timer.sol";

/// This contract represents the most simple crowdfunding campaign.
/// This contract does not protect investors from not receiving goods
/// they were promised from the crowdfunding owner. This kind of contract
/// might be suitable for campaigns that do not promise anything to the
/// investors except that they will start working on the project.
/// (e.g. almost all blockchain spinoffs.)
contract Crowdfunding {
    address private owner;
    Timer private timer;

    uint256 public goal;
    uint256 public endTimestamp;
    mapping(address => uint256) public investments;

    // We need this in order to keep track of the amount of money
    // invested; we can't iterate through mappings.
    uint256 private capital;

    constructor(
        address _owner,
        Timer _timer,
        uint256 _goal,
        uint256 _endTimestamp
    ) public {
        owner = _owner == 0 ? msg.sender : _owner;
        timer = _timer;
        goal = _goal;
        endTimestamp = _endTimestamp;
        capital = 0;
    }

    function invest() public payable {
        uint256 messageValue = msg.value;

        require(
            messageValue > 0,
            "[ReqFail] Can't invest a non-positive value."
        );
        require(
            capital < goal,
            "[ReqFail] Can't invest after the goal is reached."
        );
        require(
            timer.getTime() < endTimestamp,
            "[ReqFail] Can't invest after the funding period ends."
        );

        investments[msg.sender] += messageValue;
        capital += messageValue;
    }

    function claimFunds() public {
        require(
            msg.sender == owner,
            "[ReqFail] Only the owner can claim the capital!"
        );
        require(
            capital >= goal,
            "[ReqFail] Can't claim capital before the goal is reached."
        );
        require(
            timer.getTime() >= endTimestamp,
            "[ReqFail] Can't claim capital during the funding period."
        );

        msg.sender.transfer(capital);
    }

    function refund() public {
        uint256 investedCapital = investments[msg.sender];

        require(
            investedCapital > 0,
            "[ReqFail] No invested capital to withdraw."
        );
        require(
            capital < goal,
            "[ReqFail] Can't withdraw invested capital after the goal is reached."
        );
        require(
            timer.getTime() >= endTimestamp,
            "[ReqFail] Can't withdraw invested capital during the funding period!"
        );

        investments[msg.sender] = 0;
        msg.sender.transfer(investedCapital);
    }
}
